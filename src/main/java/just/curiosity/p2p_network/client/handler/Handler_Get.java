package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.server.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:25 PM
 */

@CMDPattern("get .*")
public class Handler_Get extends CMDHandler {
  @Override
  public void handle(String[] args, String secret) {
    try (final Socket socket = new Socket("127.0.0.1", Const.PORT)) {
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
    } catch (IOException e) {
      Logger.log(LogMsg.CANT_SEND_PACKET_TO_LOCAL_NODE, e.getMessage());
    }
  }
}
