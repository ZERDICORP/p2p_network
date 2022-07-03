package just.curiosity.p2p_network.client.zer.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

public class CMDHandlerProcessor {
  private final List<CMDHandler> handlers = new ArrayList<>();
  private static CMDHandlerProcessor instance = null;

  public static CMDHandlerProcessor getInstance() {
    if (instance == null)
      instance = new CMDHandlerProcessor();
    return instance;
  }

  public void add(CMDHandler h) {
    handlers.add(h);
  }

  public void process(String[] args) {
    for (CMDHandler handler : handlers) {
      final String value = String.join(" ", args);
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(CMDPattern.class)) {
        final CMDPattern ann = clazz.getAnnotation(CMDPattern.class);
        if (value.matches(ann.value())) {
          System.out.print("[>] enter secret: ");
          final String secret = new String(System.console().readPassword());
          if (secret.length() < 6) {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            System.out.println("[warn]: secret is too short.. min length = 6");
            return;
          }

          handler.handle(args, secret);
          break;
        }
      } else {
        System.out.println("[warn]: handler \"" + clazz.getName() + "\" has no annotation \"" +
          CMDPattern.class.getName() + "\".. skipped");
      }
    }
  }
}
