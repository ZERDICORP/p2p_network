package just.curiosity.p2p_network.core.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import just.curiosity.p2p_network.core.Server;
import just.curiosity.p2p_network.core.message.Message;
import just.curiosity.p2p_network.core.message.MessageType;

/**
 * @author zerdicorp
 * @project p2pack
 * @created 6/27/22 - 10:53 AM
 */

public class MessageHandler__Clone {
  private final MessageType type = MessageType.CLONE;

  public boolean handle(Server server, Socket socket, Message message) {
    if (!message.type().equals(type)) {
      return false;
    }

    final Set<String> nodes = server.nodes();
    try {
      final OutputStream outputStream = socket.getOutputStream();
      outputStream.write(String.join(",", nodes).getBytes());
    } catch (IOException e) {
      System.out.println("Can't write to socket output stream..");
    }

    nodes.add(socket.getInetAddress().toString() + ":" + socket.getLocalPort());

    return true;
  }
}
