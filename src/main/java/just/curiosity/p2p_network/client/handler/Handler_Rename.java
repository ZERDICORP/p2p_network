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
 * @created 7/4/22 - 9:52 AM
 */

@WithPattern("rename .* .*")
public class Handler_Rename extends Handler {
  @Override
  public void handle(String[] args, String secret, Socket socket) throws IOException {
    new Packet()
      .withType(PacketType.RENAME_DATA)
      .withPayload(secret + "\n" + args[1] + "\n" + args[2])
      .sendTo(socket);

    final Packet packet = Packet.read(socket.getInputStream());
    if (packet == null) {
      return;
    }

    if (!packet.type().equals(PacketType.OK)) {
      Logger.byPacketType(packet.type());
      return;
    }

    Logger.log(LogMsg.FILE_RENAMED_SUCCESSFULLY);
  }
}
