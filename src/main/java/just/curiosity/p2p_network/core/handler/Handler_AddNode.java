package just.curiosity.p2p_network.core.handler;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import just.curiosity.p2p_network.core.Server;
import just.curiosity.p2p_network.core.annotation.WithType;
import just.curiosity.p2p_network.core.message.Message;
import just.curiosity.p2p_network.core.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/28/22 - 12:37 PM
 */

@WithType(MessageType.ADD_NODE)
public class Handler_AddNode implements Handler {
  @Override
  public void handle(Server server, Socket socket, Message message) {
    final byte[] payload = message.payload();
    final Set<String> nodes = server.nodes();
    nodes.add(new String(payload, 0, payload.length, StandardCharsets.UTF_8));

    System.out.println("NODES AFTER ADDING NEW NODE: " + nodes);
  }
}
