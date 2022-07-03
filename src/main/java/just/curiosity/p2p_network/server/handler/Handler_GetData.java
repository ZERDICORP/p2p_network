package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;
import just.curiosity.p2p_network.server.util.AESCipher;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:28 PM
 */

@WithType(MessageType.GET_DATA)
public class Handler_GetData implements Handler {
  public void handle(Server server, Socket socket, Message message) {
    final String[] payload = new String(message.payload()).split("\n", 2);
    if (payload.length != 2) {
      return;
    }

    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
      final String fileName = DigestUtils.sha256Hex(payload[1]);
      final Set<String> nodes = server.nodes();
      final String[] shards;
      try {
        final String sharedContent = FileUtils.readFileToString(
          new File(Const.sharedDirectory + "/" + fileName), StandardCharsets.UTF_8);
        shards = sharedContent.split("\n");
      } catch (IOException e) {
        System.out.println("Can't read signature \"" + fileName + "\".. " + e);
        return;
      }

      final StringBuilder originalFileContent = new StringBuilder();
      for (String shardInfo : shards) {
        final String[] shardInfoArr = shardInfo.split(",");

        System.out.println("REQUESTING SHARD: " + fileName + "_" + shardInfoArr[0]); // TODO: remove debug log

        String shard = null;
        for (String nodeAddress : nodes) {
          try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
            nodeSocket.getOutputStream().write(new Message(MessageType.GET_DATA, (fileName + "\n" + shardInfoArr[0]).getBytes())
              .build());

            final byte[] buffer = new byte[1024]; // TODO: replace fixed buffer size
            final int size = nodeSocket.getInputStream().read(buffer);
            if (size == -1) {
              continue;
            }

            final byte[] foundShard = Arrays.copyOfRange(buffer, 0, size);
            // If the hash of the found shard does not match the
            // signature, then the shard has been modified and is
            // no longer valid. Skip and continue the search.
            if (!shardInfoArr[1].equals(DigestUtils.sha256Hex(foundShard))) {
              continue;
            }

            final byte[] decryptedFoundShard = AESCipher.decrypt(foundShard, payload[0].getBytes());
            // If the decrypt method returns null, then the wrong
            // key was used.
            if (decryptedFoundShard == null) {
              System.out.println("DECRYPT RETURNS NULL");
              return;
            }

            shard = new String(decryptedFoundShard);
            break;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        if (shard == null) {
          return;
        }

        originalFileContent.append(shard);
      }

      try {
        socket.getOutputStream().write(originalFileContent.toString().getBytes());
      } catch (IOException e) {
        System.out.println("Can't write to socket output stream.. " + e);
      }
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload[0] + socketAddress) + "_" + payload[1];
    final byte[] shard;
    try {
      shard = FileUtils.readFileToByteArray(new File(Const.shardsDirectory + "/" + shardName));
    } catch (IOException e) {
      System.out.println("Can't read shard \"" + payload[0] + "\".. " + e);
      return;
    }

    try {
      socket.getOutputStream().write(shard);
    } catch (IOException e) {
      System.out.println("Can't write to socket output stream.. " + e);
    }
  }
}
