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
import just.curiosity.p2p_network.util.AESCipher;
import just.curiosity.p2p_network.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/6/22 - 10:19 PM
 */

@WithPacketType(PacketType.GET_FILE)
@WithSocketAddress("127.0.0.1")
public class Handler_GetFile implements Handler {
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

    final StringBuilder result = new StringBuilder();
    for (String metaInfo : metaData) {
      final String[] shardInfo = metaInfo.split(",");
      final String shardName = DigestUtils.sha256Hex(metaFileName + shardInfo[0]);

      System.out.println("REQUESTING SHARD: " + shardName); // TODO: remove debug log

      String shard = null;
      for (String nodeAddress : server.nodes()) {
        final Packet getShardPacket = getShard(nodeAddress, server.port(), shardName, shardInfo[1], payload.get(0));
        if (getShardPacket == null) {
          continue;
        }

        if (!getShardPacket.type().equals(PacketType.OK)) {
          getShardPacket.sendTo(socket);
          return;
        }

        shard = getShardPacket.payloadAsString();
        break;
      }

      if (shard == null) {
        new Packet()
          .withType(PacketType.FILE_NOT_FOUND)
          .sendTo(socket);
        return;
      }

      result.append(shard);
    }

    new Packet()
      .withType(PacketType.OK)
      .withPayload(result.toString().getBytes())
      .sendTo(socket);
  }

  public static Packet getShard(String nodeAddress, int port, String shardName, String shardSignature,
                                byte[] secret) throws IOException {
    try (final Socket nodeSocket = new Socket(nodeAddress, port)) {
      new Packet()
        .withType(PacketType.GET_DATA)
        .withPayload(shardName.getBytes())
        .sendTo(nodeSocket);

      final Packet getShardPacket = Packet.read(nodeSocket.getInputStream());
      if (getShardPacket == null) {
        return null;
      }

      final byte[] foundShard = getShardPacket.payload();
      if (!shardSignature.equals(DigestUtils.sha256Hex(foundShard))) {
        return null;
      }

      final byte[] decryptedFoundShard = AESCipher.decrypt(foundShard, secret);
      if (decryptedFoundShard == null) {
        return new Packet()
          .withType(PacketType.WRONG_SECRET);
      }

      return getShardPacket.withPayload(decryptedFoundShard);
    }
  }
}
