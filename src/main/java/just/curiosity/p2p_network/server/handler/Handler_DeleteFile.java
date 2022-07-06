package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.annotation.WithSocketAddress;
import just.curiosity.p2p_network.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 10:27 PM
 */

@WithPacketType(PacketType.DELETE_FILE)
@WithSocketAddress("127.0.0.1")
public class Handler_DeleteFile implements Handler {
  @Override
  public void handle(Server server, Socket socket, String socketAddress, Packet packet) throws IOException {
    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final String metaFileName = DigestUtils.sha256Hex(payload.get(1));
    final File metaFile = new File(Const.META_DIRECTORY + "/" + metaFileName);
    final String[] metaData;
    try {
      metaData = FileUtils.readFileToString(metaFile, StandardCharsets.UTF_8).split("\n");
    } catch (IOException e) {
      new Packet()
        .withType(PacketType.FILE_NOT_FOUND)
        .sendTo(socket);
      return;
    }

    for (String metaInfo : metaData) {
      final String[] shardInfo = metaInfo.split(",");
      final String shardName = DigestUtils.sha256Hex(metaFileName + shardInfo[0]);

      for (String nodeAddress : server.nodes()) {
        final Packet getShardPacket = Handler_GetFile.getShard(nodeAddress, server.port(), shardName,
          shardInfo[1], payload.get(0));
        if (getShardPacket == null) {
          continue;
        }

        if (!getShardPacket.type().equals(PacketType.OK)) {
          getShardPacket.sendTo(socket);
          return;
        }

        try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
          new Packet()
            .withType(PacketType.DELETE_SHARD)
            .withPayload(shardName.getBytes())
            .sendTo(nodeSocket);
        }
      }
    }

    FileUtils.delete(metaFile);

    new Packet()
      .withType(PacketType.OK)
      .sendTo(socket);
  }
}
