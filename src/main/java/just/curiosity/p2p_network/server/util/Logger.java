package just.curiosity.p2p_network.server.util;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 3:47 PM
 */

public class Logger {
  public static void log(String msg) {
    log(msg, new String[]{});
  }

  public static void log(String msg, String arg) {
    log(msg, new String[]{arg});
  }

  public static void log(String msg, String[] args) {
    for (String arg : args) {
      msg = msg.replace("?", arg);
    }

    System.out.println("[log]: " + msg);
  }
}
