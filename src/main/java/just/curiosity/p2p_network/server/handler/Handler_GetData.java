package just.curiosity.p2p_network.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:28 PM
 */

@WithType(MessageType.GET_DATA)
public class Handler_GetData implements Handler {
  public void handle(Server server, Socket socket, Message message) {
    final String fileName = DigestUtils.sha256Hex(new String(message.payload(), StandardCharsets.UTF_8));
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
      System.out.println("REQUESTING DATA BY ID: " + fileName); // TODO: remove debug log
      final Set<String> nodes = server.nodes();
      final String dataSignature;
      try {
        dataSignature = server.readFromFile(
          Const.signaturesDirectory + "/" + fileName);
      } catch (IOException e) {
        System.out.println("Can't read signature \"" + fileName + "\".. " + e);
        return;
      }

      for (String nodeAddress : nodes) {
        try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
          nodeSocket.getOutputStream().write(new Message(MessageType.GET_DATA, message.payload()).build());

          final byte[] buffer = new byte[1024]; // TODO: replace fixed buffer size
          final int size = nodeSocket.getInputStream().read(buffer);
          if (size == -1) {
            continue;
          }

          final String foundData = new String(buffer, 0, size, StandardCharsets.UTF_8);
          // If the hash of the found data does not match the
          // previously stored hash, then the data has been
          // modified and is no longer valid. Skip and continue
          // the search.
          if (!dataSignature.equals(DigestUtils.sha256Hex(foundData))) {
            continue;
          }

          try {
            final OutputStream outputStream = socket.getOutputStream();
            outputStream.write(foundData.getBytes());
          } catch (IOException e) {
            System.out.println("Can't write to socket output stream.. " + e);
          }

          break;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      return;
    }

    final Map<String, String> dataStorage = server.dataStorage();
    final String data = dataStorage.get(DigestUtils.sha256Hex(fileName + socketAddress));
    if (data == null) {
      return;
    }

    try {
      final OutputStream outputStream = socket.getOutputStream();
      outputStream.write((data + ".").getBytes());
    } catch (IOException e) {
      System.out.println("Can't write to socket output stream.. " + e);
    }
  }
}
