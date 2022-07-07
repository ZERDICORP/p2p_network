package just.curiosity.p2p_network.server.handler;

import java.net.Socket;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/28/22 - 12:37 PM
 */

@WithPacketType(PacketType.ADD_NODE)
public class Handler_AddNode implements Handler {
  @Override
  public void handle(Server server, Socket socket, String socketAddress, Packet packet) {
    server.nodes()
      .add(packet.payloadAsString());
  }
}
