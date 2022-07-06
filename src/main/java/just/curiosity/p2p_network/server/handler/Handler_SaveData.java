package just.curiosity.p2p_network.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.server.util.AESCipher;
import just.curiosity.p2p_network.server.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 1:01 PM
 */

@WithPacketType(PacketType.SAVE_DATA)
public class Handler_SaveData implements Handler {
  public void handle(Server server, Socket socket, Packet packet) throws IOException {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    if (socketAddress.equals("127.0.0.1")) {
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

      shuffleArray(shards, indices);

      final String[] metaData = new String[shards.length];
      for (int i = 0; i < shards.length; i++) {
        final String shardName = DigestUtils.sha256Hex(metaFileName + i);
        final byte[] encryptedShard = AESCipher.encrypt(shards[i], payload.get(0));

        // Sending a shard to all nodes.
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          outputStream.write(shardName.getBytes());
          outputStream.write('\n');
          outputStream.write(encryptedShard);

          server.sendToAll(new Packet()
            .withType(PacketType.SAVE_DATA)
            .withPayload(outputStream.toByteArray()));
        }

        // We save information about the correct sequence of
        // shards and their contents.
        metaData[indices[i]] = i + "," + DigestUtils.sha256Hex(encryptedShard);

        System.out.println("SHARED SHARD: " + shardName); // TODO: remove debug log
      }

      FileUtils.writeByteArrayToFile(new File(Const.META_DIRECTORY + "/" + metaFileName),
        String.join("\n", metaData).getBytes());

      new Packet()
        .withType(PacketType.OK)
        .sendTo(socket);
      return;
    }

    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);
    FileUtils.writeByteArrayToFile(new File(Const.SHARDS_DIRECTORY + "/" + shardName), payload.get(1));

    System.out.println("SAVED SHARD: " + shardName); // TODO: remove debug log
  }

  private void shuffleArray(byte[][] arr, int[] indices) {
    final Random random = ThreadLocalRandom.current();
    for (int i = 0; i < arr.length; i++) {
      final int j = random.nextInt(i + 1);
      // Swap shards.
      final byte[] temp = arr[j];
      arr[j] = arr[i];
      arr[i] = temp;
      // Swap indices.
      final int tempIndex = indices[j];
      indices[j] = indices[i];
      indices[i] = tempIndex;
    }
  }
}
