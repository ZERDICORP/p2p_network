package just.curiosity.p2p_network.server.handler;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

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
      System.out.println("SHARE DATA: " + new String(message.payload(), StandardCharsets.UTF_8)); // TODO: remove debug log
      server.sendToAll(new Message(MessageType.SAVE_DATA, message.payload()));
      return;
    }

    // If the request came from another node (not from the local
    // machine), then someone is sharing data, and we need to store
    // it on the server.
    final List<String> dataStorage = server.dataStorage();
    dataStorage.add(new String(message.payload(), StandardCharsets.UTF_8));

    System.out.println("UPDATED DATA STORAGE: " + dataStorage); // TODO: remove debug log
  }
}
