package just.curiosity.p2p_network.server.handler;

import java.net.Socket;
import java.util.Set;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:53 AM
 */

@WithPacketType(PacketType.CLONE_NODES)
public class Handler_CloneNodes implements Handler {
  @Override
  public void handle(Server server, Socket socket, String socketAddress, Packet packet) {
    final Set<String> nodes = server.nodes();
    nodes.remove(socketAddress);

    new Packet()
      .withType(PacketType.OK)
      .withPayload(String.join(",", nodes).getBytes())
      .sendTo(socket);

    // Notifying the nodes that a new node has connected
    // and needs to be added to the list of nodes.
    server.sendToAll(new Packet()
      .withType(PacketType.ADD_NODE)
      .withPayload(socketAddress.getBytes()));

    nodes.add(socketAddress);

    System.out.println("CLONE ACCEPTED: " + socketAddress); // TODO: remove debug log
  }
}
