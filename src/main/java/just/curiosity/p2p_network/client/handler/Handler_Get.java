package just.curiosity.p2p_network.client.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.client.annotation.WithPattern;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.util.Logger;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:25 PM
 */

@WithPattern("cat ([^\\s]+|[^\\s]+ -o [^\\s]+)")
public class Handler_Get extends Handler {
  @Override
  public void handle(String[] args, String secret, Socket socket) throws IOException {
    new Packet()
      .withType(PacketType.GET_FILE)
      .withPayload(secret + "\n" + args[1])
      .sendTo(socket);

    final Packet packet = Packet.read(socket.getInputStream());
    if (packet == null) {
      return;
    }

    if (!packet.type().equals(PacketType.OK)) {
      Logger.byPacketType(packet.type());
      return;
    }

    if (args.length == 2) {
      System.out.println(packet.payloadAsString());
      return;
    }

    final File outFile = new File(args[3]);
    FileUtils.writeByteArrayToFile(outFile, packet.payload());
    Logger.log(LogMsg.FILE_CONTENTS_ARE_WRITTEN_TO, outFile.getPath());
  }
}
