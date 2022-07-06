package just.curiosity.p2p_network.client.handler;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

public abstract class Handler {
  public abstract void handle(String[] args, String secret);
}
