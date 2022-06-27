package just.curiosity.p2p_network;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import just.curiosity.p2p_network.core.message.MessageType;

/**
 * @author zerdicorp
 * @project p2pack
 * @created 6/27/22 - 10:44 AM
 */

public class TestClient {
  public static void main(String[] args) throws IOException {
    try (final Socket socket = new Socket("188.187.188.37", 8080)) {
      final String payload = "Hello, world!";

      final String message = MessageType.CLONE + "\n" +
        payload.length() + "\n" +
        payload;

      socket.getOutputStream().write(message.getBytes());

      final byte[] buffer = new byte[1024];
      final int size = socket.getInputStream().read(buffer);

      System.out.println(new String(buffer, 0, size, StandardCharsets.UTF_8));
    }
  }
}
