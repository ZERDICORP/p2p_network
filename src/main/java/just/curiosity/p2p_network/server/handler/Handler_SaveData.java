package just.curiosity.p2p_network.server.handler;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
    final String[] payload = new String(message.payload(), StandardCharsets.UTF_8).split(":");
    if (payload.length != 2) {
      return;
    }

    // If the request to save data was sent from the local machine,
    // then you need to share this data between all nodes.
    if (socketAddress.equals("127.0.0.1")) {
      System.out.println("SHARE DATA: " + new String(message.payload(), StandardCharsets.UTF_8)); // TODO: remove debug log
      server.sendToAll(new Message(MessageType.SAVE_DATA, message.payload()));

      // We must save the hash of the data so that when we later
      // receive it from the network by identifier, we can compare
      // it with the hash of the received data, thereby determining
      // whether someone has changed the data or not.
      final Map<String, String> sharedDataSignature = server.sharedDataSignature();
      sharedDataSignature.put(payload[0], DigestUtils.sha256Hex(payload[1]));
      return;
    }

    // If the request came from another node (not from the local
    // machine), then someone is sharing data, and we need to store
    // it on the server.
    final Map<String, String> dataStorage = server.dataStorage();
    dataStorage.put(DigestUtils.sha256Hex(payload[0] + socketAddress), payload[1]);

    System.out.println("UPDATED DATA STORAGE: " + dataStorage); // TODO: remove debug log
  }
}
