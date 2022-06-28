package just.curiosity.p2p_network;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import just.curiosity.p2p_network.core.Server;
import just.curiosity.p2p_network.core.message.Message;
import just.curiosity.p2p_network.core.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:02 AM
 */

public class Main {
  private static Set<String> cloneNodes(String rootNodeAddress) throws IOException {
    final String[] hostAndPort = rootNodeAddress.split(":");
    Set<String> nodes = new HashSet<>();
    try (final Socket socket = new Socket(hostAndPort[0], Integer.parseInt(hostAndPort[1]))) {
      socket.getOutputStream().write(new Message(MessageType.CLONE).build());

      final byte[] buffer = new byte[1024];
      final int size = socket.getInputStream().read(buffer);
      if (size == -1) {
        return nodes;
      }

      nodes = new HashSet<>(
        Arrays.asList(new String(buffer, 0, size, StandardCharsets.UTF_8).split(",")));
    }
    return nodes;
  }

  public static void main(String[] args) {
    int port = 8080;
    if (args.length >= 2) {
      port = Integer.parseInt(args[1]);
    }

    final Server server = new Server(port);
    try {
      if (args.length > 0) {
        server.setNodes(cloneNodes(args[0]));
      }
      server.start();
    } catch (IOException e) {
      System.out.println("Can't start server.. " + e.getMessage());
    }
  }
}
