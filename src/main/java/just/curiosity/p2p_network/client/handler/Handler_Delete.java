package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.annotation.WithPattern;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:19 AM
 */

@WithPattern("rm [^\\s]+")
public class Handler_Delete extends Handler {
  @Override
  public void handle(String[] args, String secret, Socket socket) throws IOException {
    new Packet()
      .withType(PacketType.DELETE_FILE)
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

    Logger.log(LogMsg.FILE_DELETED_SUCCESSFULLY);
  }
}
