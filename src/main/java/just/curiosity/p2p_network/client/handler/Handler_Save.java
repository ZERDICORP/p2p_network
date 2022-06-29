package just.curiosity.p2p_network.client.handler;

import just.curiosity.p2p_network.client.zer.cmd.CMDHandler;
import just.curiosity.p2p_network.client.zer.cmd.CMDPattern;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

@CMDPattern("save [a-zA-Z0-9._-]{1,255} .*")
public class Handler_Save extends CMDHandler {
  @Override
  public void handle(String[] args) {
    for (String arg : args) {
      System.out.println("arg: " + arg);
    }
  }
}