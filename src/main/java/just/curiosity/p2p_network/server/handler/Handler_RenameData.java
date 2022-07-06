package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:55 AM
 */

@WithPacketType(PacketType.RENAME_DATA)
public class Handler_RenameData implements Handler {
  public void handle(Server server, Socket socket, Packet packet) throws IOException {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    if (socketAddress.equals("127.0.0.1")) {
      final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 3);
      if (payload.size() != 3) {
        return;
      }

      final String metaFileName = DigestUtils.sha256Hex(payload.get(1));
      final String newMetaFileName = DigestUtils.sha256Hex(payload.get(2));
      final File metaFile = new File(Const.META_DIRECTORY + "/" + metaFileName);
      final File newMetaFile = new File(Const.META_DIRECTORY + "/" + newMetaFileName);
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
        final String newShardName = DigestUtils.sha256Hex(newMetaFileName + shardInfo[0]);

        System.out.println("RENAMING SHARD: " + shardName + " -> " + newShardName); // TODO: remove debug log

        for (String nodeAddress : server.nodes()) {
          final Packet getShardPacket = Handler_GetData.getShard(nodeAddress, server.port(), shardName,
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
              .withType(PacketType.RENAME_DATA)
              .withPayload(shardName + "\n" + newShardName)
              .sendTo(nodeSocket);
          }
        }
      }

      FileUtils.moveFile(metaFile, newMetaFile);
      return;
    }

    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);
    final String newShardName = DigestUtils.sha256Hex(payload.getAsString(1) + socketAddress);
    final File shardFile = new File(Const.SHARDS_DIRECTORY + "/" + shardName);
    final File newShardFile = new File(Const.SHARDS_DIRECTORY + "/" + newShardName);

    FileUtils.moveFile(shardFile, newShardFile);

    System.out.println("RENAMED SHARD: " + shardName + " -> " + newShardName); // TODO: remove debug log
  }
}
