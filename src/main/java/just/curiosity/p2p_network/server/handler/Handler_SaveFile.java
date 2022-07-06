package just.curiosity.p2p_network.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.annotation.WithSocketAddress;
import just.curiosity.p2p_network.util.AESCipher;
import just.curiosity.p2p_network.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 10:32 PM
 */

@WithPacketType(PacketType.SAVE_FILE)
@WithSocketAddress("127.0.0.1")
public class Handler_SaveFile implements Handler {
  @Override
  public void handle(Server server, Socket socket, String socketAddress, Packet packet) throws IOException {
    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final File sourceFile = new File(payload.getAsString(1));
    final byte[] sourceData;
    try {
      sourceData = FileUtils.readFileToByteArray(sourceFile);
    } catch (IOException e) {
      new Packet()
        .withType(PacketType.FILE_NOT_FOUND)
        .sendTo(socket);
      return;
    }

    final String metaFileName = DigestUtils.sha256Hex(sourceFile.getName());
    final byte[][] shards = new byte[(int) Math.ceil((double) sourceData.length / Const.SHARD_SIZE)][Const.SHARD_SIZE];
    final int[] indices = new int[shards.length];
    for (int i = 0; i < sourceData.length; i += Const.SHARD_SIZE) {
      final int shardIndex = i / Const.SHARD_SIZE;
      for (int j = 0; j < Const.SHARD_SIZE && j + i < sourceData.length; j++) {
        shards[shardIndex][j] = sourceData[j + i];
      }
      // We fill in the list of indices in order to know the
      // correct sequence of shards.
      indices[shardIndex] = shardIndex;
    }

    // Shuffle shards.
    final Random random = ThreadLocalRandom.current();
    for (int i = 0; i < shards.length; i++) {
      final int j = random.nextInt(i + 1);
      // Swap shards.
      final byte[] temp = shards[j];
      shards[j] = shards[i];
      shards[i] = temp;
      // Swap indices.
      final int tempIndex = indices[j];
      indices[j] = indices[i];
      indices[i] = tempIndex;
    }

    final String[] metaData = new String[shards.length];
    for (int i = 0; i < shards.length; i++) {
      byte[] shard = shards[i];
      if (indices[i] == shards.length - 1) {
        final int offset = sourceData.length % Const.SHARD_SIZE;
        if (offset > 0) {
          shard = Arrays.copyOfRange(shards[i], 0, offset);
        }
      }

      final String shardName = DigestUtils.sha256Hex(metaFileName + i);
      final byte[] encryptedShard = AESCipher.encrypt(shard, payload.get(0));

      // Sending a shard to all nodes.
      try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        outputStream.write(shardName.getBytes());
        outputStream.write('\n');
        outputStream.write(encryptedShard);

        server.sendToAll(new Packet()
          .withType(PacketType.SAVE_SHARD)
          .withPayload(outputStream.toByteArray()));
      }

      // We save information about the correct sequence of
      // shards and their contents.
      metaData[indices[i]] = i + "," + DigestUtils.sha256Hex(encryptedShard);
    }

    FileUtils.writeByteArrayToFile(new File(Const.META_DIRECTORY + "/" + metaFileName),
      String.join("\n", metaData).getBytes());

    new Packet()
      .withType(PacketType.OK)
      .sendTo(socket);
  }
}
