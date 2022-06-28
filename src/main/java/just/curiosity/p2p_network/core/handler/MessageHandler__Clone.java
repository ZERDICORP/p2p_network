package just.curiosity.p2p_network.core.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import just.curiosity.p2p_network.core.Server;
import just.curiosity.p2p_network.core.annotation.WithType;
import just.curiosity.p2p_network.core.message.Message;
import just.curiosity.p2p_network.core.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:53 AM
 */

@WithType(MessageType.CLONE)
public class MessageHandler__Clone implements Handler {
  public boolean handle(Server server, Socket socket, Message message) {
    final Set<String> nodes = server.nodes();
    nodes.remove(socket.getInetAddress().toString());
    try {
      final OutputStream outputStream = socket.getOutputStream();
      outputStream.write(String.join(",", nodes).getBytes());
    } catch (IOException e) {
      System.out.println("Can't write to socket output stream..");
    }

    // TODO: notify all nodes about new node

    nodes.add(socket.getInetAddress().toString());

    System.out.println("CLONE ACCEPTED: " + socket.getInetAddress());

    return true;
  }
}
