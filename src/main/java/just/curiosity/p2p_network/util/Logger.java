package just.curiosity.p2p_network.util;

import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;

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

  public static void byPacketType(PacketType packetType) {
    switch (packetType) {
      case WRONG_SECRET -> log(LogMsg.WRONG_SECRET);
      case FILE_NOT_FOUND -> log(LogMsg.FILE_NOT_FOUND);
    }
  }
}
