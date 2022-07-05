package just.curiosity.p2p_network.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.constants.PacketType;
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
  public void handle(Server server, Socket socket, Packet packet) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    // If the request to save data was sent from the local machine,
    // then you need to share this data between all nodes.
    if (socketAddress.equals("127.0.0.1")) {
      final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
      if (payload.size() != 2) {
        return;
      }

      final File file = new File(payload.getAsString(1));
      final byte[] data;
      try {
        data = FileUtils.readFileToByteArray(file);
      } catch (IOException e) {
        System.out.println("Can't read file \"" + file.getPath() + "\".. " + e);
        return;
      }

      final String fileNameHash = DigestUtils.sha256Hex(file.getName());
      final byte[][] shards = new byte[(int) Math.ceil((double) data.length / Const.SHARD_SIZE)][Const.SHARD_SIZE];
      final int[] indices = new int[shards.length];
      for (int i = 0; i < data.length; i += Const.SHARD_SIZE) {
        final int shardIndex = i / Const.SHARD_SIZE;
        for (int j = 0; j < Const.SHARD_SIZE && j + i < data.length; j++) {
          shards[shardIndex][j] = data[j + i];
        }
        // We fill in the list of indices in order to know the
        // correct sequence of shards.
        indices[shardIndex] = shardIndex;
      }

      shuffleArray(shards, indices);

      final String[] shardsInfo = new String[shards.length];
      for (int i = 0; i < shards.length; i++) {
        final String shardName = DigestUtils.sha256Hex(fileNameHash + i);
        final byte[] encryptedShard = AESCipher.encrypt(shards[i], payload.get(0));

        // Sending a shard to all nodes.
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          outputStream.write(shardName.getBytes());
          outputStream.write('\n');
          outputStream.write(encryptedShard);
          server.sendToAll(new Packet(PacketType.SAVE_DATA, outputStream.toByteArray()));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        // We save information about the correct sequence of
        // shards and their contents.
        shardsInfo[indices[i]] = i + "," + DigestUtils.sha256Hex(encryptedShard);

        System.out.println("SHARED SHARD: " + shardName); // TODO: remove debug log
      }

      try (final FileOutputStream out = new FileOutputStream(Const.META_DIRECTORY + "/" + fileNameHash)) {
        out.write(String.join("\n", shardsInfo).getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return;
    }

    // If the request came from another node (not from the local
    // machine), then someone shared the shard, and we need to
    // save it to disk.

    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);

    try {
      FileUtils.writeByteArrayToFile(new File(Const.SHARDS_DIRECTORY + "/" + shardName), payload.get(1));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

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
