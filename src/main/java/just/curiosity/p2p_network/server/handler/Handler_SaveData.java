package just.curiosity.p2p_network.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 1:01 PM
 */

@WithType(MessageType.SAVE_DATA)
public class Handler_SaveData implements Handler {
  public void handle(Server server, Socket socket, Message message) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    // If the request to save data was sent from the local machine,
    // then you need to share this data between all nodes.
    if (socketAddress.equals("127.0.0.1")) {
      final String pathToFile = new String(message.payload(), StandardCharsets.UTF_8);
      String fileContent;
      try {
        fileContent = server.readFromFile(pathToFile);
      } catch (IOException e) {
        System.out.println("Can't read file \"" + pathToFile + "\".. " + e);
        return;
      }

      final String fileNameHash = DigestUtils.sha256Hex(new File(pathToFile).getName());
      final byte[] data = fileContent.getBytes();
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

      try (final FileOutputStream out = new FileOutputStream(Const.sharedDirectory + "/" + fileNameHash)) {
        for (int i = 0; i < shards.length; i++) {
          // Sending a shard to all nodes.
          try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(fileNameHash.getBytes());
            outputStream.write('\n');
            outputStream.write(shards[i]);
            server.sendToAll(new Message(MessageType.SAVE_DATA, outputStream.toByteArray()));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          // We save information about the correct sequence of
          // shards and their contents.
          out.write(String.valueOf(indices[i]).getBytes());
          out.write(',');
          out.write(DigestUtils.sha256Hex(shards[i]).getBytes());

          if (i < shards.length - 1) {
            out.write('\n');
          }

          System.out.println("SHARED SHARD: " + DigestUtils.sha256Hex(new File(pathToFile).getName()) + "_" + i); // TODO: remove debug log
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return;
    }

    // If the request came from another node (not from the local
    // machine), then someone shared the shard, and we need to
    // save it to disk.

    final String[] payload = new String(message.payload(), StandardCharsets.UTF_8).split("\n", 2);
    if (payload.length != 2) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload[0] + socketAddress);
    final File shardsDirectory = new File(Const.shardsDirectory);
    final File[] shards = shardsDirectory.listFiles((d, name) -> name.startsWith(shardName));
    if (shards == null) {
      return;
    }

    server.writeToFile(Const.shardsDirectory + "/" + shardName + "_" + shards.length, payload[1]);

    System.out.println("SAVED SHARD: " + shardName + "_" + shards.length); // TODO: remove debug log
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
