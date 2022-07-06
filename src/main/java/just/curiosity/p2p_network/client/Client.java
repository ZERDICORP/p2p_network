package just.curiosity.p2p_network.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import just.curiosity.p2p_network.client.annotation.WithPattern;
import just.curiosity.p2p_network.client.handler.Handler;
import just.curiosity.p2p_network.client.handler.Handler_Delete;
import just.curiosity.p2p_network.client.handler.Handler_Get;
import just.curiosity.p2p_network.client.handler.Handler_Rename;
import just.curiosity.p2p_network.client.handler.Handler_Save;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:26 PM
 */

public class Client {
  private final List<Handler> handlers = new ArrayList<>();

  {
    handlers.add(new Handler_Save());
    handlers.add(new Handler_Get());
    handlers.add(new Handler_Delete());
    handlers.add(new Handler_Rename());
  }

  public void handle(String[] args) {
    for (Handler handler : handlers) {
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(WithPattern.class)) {
        final WithPattern ann = clazz.getAnnotation(WithPattern.class);
        if (String.join(" ", args).matches(ann.value())) {
          System.out.print("[>] enter secret: ");
          final String secret = new String(System.console().readPassword());
          if (secret.length() < 6) {
            try {
              TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            Logger.log(LogMsg.SECRET_IS_TOO_SHORT);
            return;
          }

          try (final Socket socket = new Socket("127.0.0.1", Const.PORT)) {
            handler.handle(args, secret, socket);
          } catch (IOException e) {
            Logger.log(LogMsg.ERROR_SENDING_PACKET_TO_LOCAL_NODE, e.getMessage());
          }
        } else {
          Logger.log(LogMsg.HANDLER_HAS_NO_ANNOTATION, new String[]{
            clazz.getName(),
            WithPattern.class.getName()});
        }
      }
    }
  }
}