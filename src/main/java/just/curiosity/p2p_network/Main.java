package just.curiosity.p2p_network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import just.curiosity.p2p_network.client.Client;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:02 AM
 */

public class Main {
  static {
    try {
      Files.createDirectories(Paths.get(Const.SHARDS_DIRECTORY));
      Files.createDirectories(Paths.get(Const.META_DIRECTORY));
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO: replace exception with log
    }
  }

  private static void startClient(String[] args) {
    if (args.length < 1) {
      Logger.log(LogMsg.WRONG_USAGE);
      return;
    }

    final Client client = new Client();
    client.handle(Arrays.copyOfRange(args, 1, args.length));
  }

  private static void startServer(String[] args) {
    final Server server = new Server(Const.PORT);
    try {
      if (args.length > 1) {
        server.cloneNodes(args[1]);
      }
      server.start();
    } catch (IOException e) {
      Logger.log(LogMsg.CANT_START_SERVER, e.getMessage());
    }
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      Logger.log(LogMsg.WRONG_USAGE);
      return;
    }

    switch (args[0]) {
      case "-c" -> startClient(args);
      case "-s" -> startServer(args);
    }
  }
}
