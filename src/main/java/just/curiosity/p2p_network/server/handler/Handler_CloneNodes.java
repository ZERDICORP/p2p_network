package just.curiosity.p2p_network.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:53 AM
 */

@WithType(MessageType.CLONE_NODES)
public class Handler_CloneNodes implements Handler {
  public void handle(Server server, Socket socket, Message message) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    final Set<String> nodes = server.nodes();
    nodes.remove(socketAddress);
    try {
      final OutputStream outputStream = socket.getOutputStream();
      outputStream.write(String.join(",", nodes).getBytes());
    } catch (IOException e) {
      System.out.println("Can't write to socket output stream..");
    }

    System.out.println("CLONE ACCEPTED: " + socketAddress);

    // Notifying the nodes that a new node has connected
    // and needs to be added to the list of nodes.
    server.sendToAll(new Message(MessageType.ADD_NODE, socketAddress.getBytes()));

    nodes.add(socketAddress);
  }
}
