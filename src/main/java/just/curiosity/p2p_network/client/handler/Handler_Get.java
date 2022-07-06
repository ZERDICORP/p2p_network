package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.annotation.WithPattern;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:25 PM
 */

@WithPattern("get .*")
public class Handler_Get extends Handler {
  @Override
  public void handle(String[] args, String secret, Socket socket) throws IOException {
    new Packet()
      .withType(PacketType.GET_DATA)
      .withPayload(secret + "\n" + args[1])
      .sendTo(socket);

    final Packet packet = Packet.read(socket.getInputStream());
    if (packet == null) {
      return;
    }

    if (!packet.type().equals(PacketType.OK)) {
      Logger.byPacketType(packet.type());
      return;
    }

    System.out.println(packet.payloadAsString());
  }
}
