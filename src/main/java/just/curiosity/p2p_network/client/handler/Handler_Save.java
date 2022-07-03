package just.curiosity.p2p_network.client.handler;

import java.io.File;
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

@CMDPattern("save .*")
public class Handler_Save extends CMDHandler {
  @Override
  public void handle(String[] args, String secret) {
    if (!new File(args[1]).exists()) {
      System.out.println("File \"" + args[1] + "\" does not exist..");
      return;
    }

    try (final Socket nodeSocket = new Socket("127.0.0.1", Const.PORT)) {
      final OutputStream outputStream = nodeSocket.getOutputStream();
      outputStream.write(new Message(MessageType.SAVE_DATA, args[1].getBytes()).build());
    } catch (IOException e) {
      System.out.println("Can't send message to local node.. " + e);
    }
  }
}