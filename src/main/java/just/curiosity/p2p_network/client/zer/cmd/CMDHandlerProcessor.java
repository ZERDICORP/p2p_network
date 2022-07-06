package just.curiosity.p2p_network.client.zer.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import just.curiosity.p2p_network.client.handler.Handler;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.server.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

public class CMDHandlerProcessor {
  private final List<Handler> handlers = new ArrayList<>();
  private static CMDHandlerProcessor instance = null;

  public static CMDHandlerProcessor getInstance() {
    if (instance == null)
      instance = new CMDHandlerProcessor();
    return instance;
  }

  public void add(Handler h) {
    handlers.add(h);
  }

  public void process(String[] args) throws InterruptedException {
    for (Handler handler : handlers) {
      final String value = String.join(" ", args);
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(CMDPattern.class)) {
        final CMDPattern ann = clazz.getAnnotation(CMDPattern.class);
        if (value.matches(ann.value())) {
          System.out.print("[>] enter secret: ");
          final String secret = new String(System.console().readPassword());
          if (secret.length() < 6) {
            TimeUnit.SECONDS.sleep(1);
            Logger.log(LogMsg.SECRET_IS_TOO_SHORT);
            return;
          }

          handler.handle(args, secret);
          break;
        }
      } else {
        Logger.log(LogMsg.HANDLER_HAS_NO_ANNOTATION, new String[]{
          clazz.getName(),
          CMDPattern.class.getName()});
      }
    }
  }
}
