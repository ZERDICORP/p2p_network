package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

@CMDPattern("save [a-zA-Z0-9._-]{1,255} .*")
public class Handler_Save extends CMDHandler {
  @Override
  public void handle(String[] args) {
    // id:data
    final String payload = args[1] + ":" + args[2];
    try (final Socket nodeSocket = new Socket("127.0.0.1", Const.PORT)) {
      final OutputStream outputStream = nodeSocket.getOutputStream();
      outputStream.write(new Message(MessageType.SAVE_DATA, payload.getBytes()).build());
    } catch (IOException e) {
      System.out.println("Can't send message to local node.. " + e);
    }
  }
}