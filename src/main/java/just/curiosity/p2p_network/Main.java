package just.curiosity.p2p_network;

import java.io.IOException;
import java.util.Arrays;
import just.curiosity.p2p_network.client.Client;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:02 AM
 */

public class Main {
  private static void startClient(String[] args) {
    if (args.length < 1) {
      System.out.println("Wrong usage.. Check out usage guide!");
      return;
    }

    final Client client = new Client();
    client.handle(Arrays.copyOfRange(args, 1, args.length));
  }

  private static void startServer(String[] args) {
    final Server server = new Server(Const.PORT);;
    try {
      if (args.length > 1) {
        server.cloneNodes(args[1]);
      }
      server.start();
    } catch (IOException e) {
      System.out.println("Can't start server.. " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Not enough parameters.. Check out the little usage guide in README.md");
      return;
    }

    switch (args[0]) {
      case "-c" -> startClient(args);
      case "-s" -> startServer(args);
    }
  }
}
