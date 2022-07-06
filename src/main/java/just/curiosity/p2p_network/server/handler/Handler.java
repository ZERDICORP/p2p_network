package just.curiosity.p2p_network.server.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/28/22 - 12:11 PM
 */

public interface Handler {
  void handle(Server server, Socket socket, String socketAddress, Packet packet) throws IOException;
}
