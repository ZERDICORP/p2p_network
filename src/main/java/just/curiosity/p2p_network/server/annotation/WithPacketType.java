package just.curiosity.p2p_network.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import just.curiosity.p2p_network.server.packet.PacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/28/22 - 12:07 PM
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithPacketType {
  PacketType value();
}