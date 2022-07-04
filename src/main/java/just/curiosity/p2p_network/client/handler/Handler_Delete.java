package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:19 AM
 */

@CMDPattern("del .*")
public class Handler_Delete extends CMDHandler {
  @Override
  public void handle(String[] args, String secret) {
    final String payload = secret + "\n" + args[1];
    try (final Socket nodeSocket = new Socket("127.0.0.1", Const.PORT)) {
      nodeSocket.getOutputStream()
        .write(new Message(MessageType.DELETE_DATA, payload.getBytes()).build());
    } catch (IOException e) {
      System.out.println("Can't send message to local node.. " + e);
    }
  }
}
