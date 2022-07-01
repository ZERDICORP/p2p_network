package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
      server.sendToAll(new Message(MessageType.SAVE_DATA,
        (fileNameHash + "\n" + fileContent).getBytes()));

      // We must save the hash of the data so that when we later
      // receive it from the network by identifier, we can compare
      // it with the hash of the received data, thereby determining
      // whether someone has changed the data or not.
      server.writeToFile(Const.signaturesDirectory + "/" + fileNameHash,
        DigestUtils.sha256Hex(fileContent));

      System.out.println("SHARED SHARD: " + fileNameHash); // TODO: remove debug log
      return;
    }

    final String[] payload = new String(message.payload(), StandardCharsets.UTF_8).split("\n", 2);
    if (payload.length != 2) {
      return;
    }

    // If the request came from another node (not from the local
    // machine), then someone is sharing data, and we need to store
    // it on the server.
    server.writeToFile(Const.shardsDirectory + "/" + DigestUtils.sha256Hex(payload[0] + socketAddress),
      payload[1]);

    System.out.println("SAVED SHARD: " + DigestUtils.sha256Hex(payload[0] + socketAddress)); // TODO: remove debug log
  }
}
