package just.curiosity.p2p_network.client;

import just.curiosity.p2p_network.client.handler.Handler_Delete;
import just.curiosity.p2p_network.client.handler.Handler_Get;
import just.curiosity.p2p_network.client.handler.Handler_Rename;
import just.curiosity.p2p_network.client.handler.Handler_Save;
import just.curiosity.p2p_network.client.zer.cmd.CMDHandlerProcessor;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:26 PM
 */

public class Client {
  public void handle(String[] args) {
    final CMDHandlerProcessor processor = CMDHandlerProcessor.getInstance();

    processor.add(new Handler_Save());
    processor.add(new Handler_Get());
    processor.add(new Handler_Delete());
    processor.add(new Handler_Rename());

    processor.process(args);
  }
}
