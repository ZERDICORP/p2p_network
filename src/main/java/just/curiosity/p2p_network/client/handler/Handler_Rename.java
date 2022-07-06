package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.annotation.ArgsPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:52 AM
 */

@ArgsPattern("rename .* .*")
public class Handler_Rename extends Handler {
  @Override
  public void handle(String[] args, String secret) {
    try (final Socket socket = new Socket("127.0.0.1", Const.PORT)) {
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
    } catch (IOException e) {
      Logger.log(LogMsg.CANT_SEND_PACKET_TO_LOCAL_NODE, e.getMessage());
    }
  }
}
