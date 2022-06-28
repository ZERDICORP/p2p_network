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
  private static final Server server = new Server(8080);

  private static Set<String> cloneNodes(String rootNodeAddress) throws IOException {
    final Set<String> nodes = new HashSet<>();
    nodes.add(rootNodeAddress);
    try (final Socket socket = new Socket(rootNodeAddress, server.port())) {
      socket.getOutputStream().write(new Message(MessageType.CLONE_NODES).build());

      final byte[] buffer = new byte[1024];
      final int size = socket.getInputStream().read(buffer);
      if (size == -1) {
        return nodes;
      }

      nodes.addAll(Arrays.asList(new String(buffer, 0, size, StandardCharsets.UTF_8).split(",")));
    }
    return nodes;
  }

  public static void main(String[] args) {
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
