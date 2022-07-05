package just.curiosity.p2p_network.server.handler;

import java.net.Socket;
import java.util.Set;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.constants.PacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/28/22 - 12:37 PM
 */

@WithPacketType(PacketType.ADD_NODE)
public class Handler_AddNode implements Handler {
  @Override
  public void handle(Server server, Socket socket, Packet packet) {
    final byte[] payload = packet.payload();
    final Set<String> nodes = server.nodes();
    nodes.add(new String(payload, 0, payload.length));

    System.out.println("NODES AFTER ADDING NEW NODE: " + nodes); // TODO: remove debug log
  }
}
