package just.curiosity.p2p_network.client.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:25 PM
 */

@CMDPattern("get .*")
public class Handler_Get extends CMDHandler {
  @Override
  public void handle(String[] args, String secret) {
    try (final Socket nodeSocket = new Socket("127.0.0.1", Const.PORT)) {
      final OutputStream outputStream = nodeSocket.getOutputStream();
      outputStream.write(new Message(MessageType.GET_DATA, args[1].getBytes()).build());

      final byte[] buffer = new byte[1024]; // TODO: replace fixed buffer size
      final int size = nodeSocket.getInputStream().read(buffer);
      if (size == -1) {
        System.out.println("File \"" + args[1] + "\" not found..");
        return;
      }

      System.out.println("File \"" + args[1] + "\" was found: " +
        new String(buffer, 0, size, StandardCharsets.UTF_8));
    } catch (IOException e) {
      System.out.println("Can't send message to local node.. " + e);
    }
  }
}
