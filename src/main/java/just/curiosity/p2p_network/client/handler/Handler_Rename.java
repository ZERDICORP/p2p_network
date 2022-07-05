package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.constants.PacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:52 AM
 */

@CMDPattern("rename .* .*")
public class Handler_Rename extends CMDHandler {
  @Override
  public void handle(String[] args, String secret) {
    final String payload = secret + "\n" + args[1] + "\n" + args[2];
    try (final Socket nodeSocket = new Socket("127.0.0.1", Const.PORT)) {
      nodeSocket.getOutputStream()
        .write(new Packet(PacketType.RENAME_DATA, payload.getBytes()).build());
    } catch (IOException e) {
      System.out.println("Can't send message to local node.. " + e);
    }
  }
}
