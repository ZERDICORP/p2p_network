package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:55 AM
 */

@WithPacketType(PacketType.RENAME_SHARD)
public class Handler_RenameShard implements Handler {
  @Override
  public void handle(Server server, Socket socket, String socketAddress, Packet packet) throws IOException {
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
