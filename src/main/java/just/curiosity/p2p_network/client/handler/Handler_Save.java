package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.annotation.ArgsPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.server.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

@ArgsPattern("save .*")
public class Handler_Save extends Handler {
  @Override
  public void handle(String[] args, String secret) {
    try (final Socket socket = new Socket("127.0.0.1", Const.PORT)) {
      new Packet()
        .withType(PacketType.SAVE_DATA)
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

      Logger.log(LogMsg.FILE_CREATED_SUCCESSFULLY);
    } catch (IOException e) {
      Logger.log(LogMsg.CANT_SEND_PACKET_TO_LOCAL_NODE, e.getMessage());
    }
  }
}