package just.curiosity.p2p_network.client.zer.cmd;

import java.util.ArrayList;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/29/22 - 12:34 PM
 */

public class CMDHandlerProcessor {
  private static CMDHandlerProcessor instance = null;

  public static CMDHandlerProcessor getInstance() {
    if (instance == null)
      instance = new CMDHandlerProcessor();
    return instance;
  }

  ArrayList<CMDHandler> handlers = new ArrayList<>();

  public void add(CMDHandler h) {
    this.handlers.add(h);
  }

  public void process(String[] args) {
    for (CMDHandler handler : this.handlers) {
      String value = String.join(" ", args);

      Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(CMDPattern.class)) {
        CMDPattern ann = clazz.getAnnotation(CMDPattern.class);
        if (value.matches(ann.value())) {
          handler.handle(args);
          break;
        }
      } else
        System.out.println("[warn]: handler \"" + clazz.getName() + "\" has no annotation \"" + CMDPattern.class.getName() + "\".. skipped");
    }
  }
}
