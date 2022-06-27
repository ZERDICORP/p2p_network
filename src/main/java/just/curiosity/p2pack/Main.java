package just.curiosity.p2pack;

import java.io.IOException;
import just.curiosity.p2pack.core.Server;

/**
 * @author zerdicorp
 * @project p2pack
 * @created 6/27/22 - 10:02 AM
 */

public class Main {
  public static void main(String[] args) {
    final Server server = new Server(Integer.parseInt(args[0]));
    try {
      server.start();
    } catch (IOException e) {
      System.out.println("Can't start server.. " + e.getMessage());
    }
  }
}
