package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;
import just.curiosity.p2p_network.server.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:21 AM
 */

@WithType(MessageType.DELETE_DATA)
public class Handler_DeleteData implements Handler {
  public void handle(Server server, Socket socket, Message message) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
      final ByteArraySplitter payload = new ByteArraySplitter(message.payload(), (byte) '\n', 2);
      if (payload.size() != 2) {
        return;
      }

      final String fileName = DigestUtils.sha256Hex(payload.get(1));
      final File sharedFile = new File(Const.sharedDirectory + "/" + fileName);
      final Set<String> nodes = server.nodes();
      final String[] shards;
      try {
        final String sharedContent = FileUtils.readFileToString(sharedFile, StandardCharsets.UTF_8);
        shards = sharedContent.split("\n");
        FileUtils.delete(sharedFile);
      } catch (IOException e) {
        System.out.println("Can't read shared \"" + fileName + "\".. " + e);
        return;
      }

      for (String shardInfo : shards) {
        final String[] shardInfoArr = shardInfo.split(",");
        final String shardName = DigestUtils.sha256Hex(fileName + shardInfoArr[0]);

        System.out.println("DELETING SHARD: " + shardName); // TODO: remove debug log

        for (String nodeAddress : nodes) {
          try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
            nodeSocket.getOutputStream()
              .write(new Message(MessageType.DELETE_DATA, shardName.getBytes()).build());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
      return;
    }

    final ByteArraySplitter payload = new ByteArraySplitter(message.payload(), (byte) '\n', 1);
    if (payload.size() != 1) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);

    try {
      FileUtils.delete(new File(Const.shardsDirectory + "/" + shardName));
      System.out.println("DELETED SHARD: " + shardName); // TODO: remove debug log
    } catch (IOException e) {
      System.out.println("Can't delete shard \"" + shardName + "\".. " + e);
    }
  }
}
