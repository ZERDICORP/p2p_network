package just.curiosity.p2p_network.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 10:23 PM
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithSocketAddress {
  String value();
}