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
import just.curiosity.p2p_network.server.util.AESCipher;
import just.curiosity.p2p_network.server.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/30/22 - 12:28 PM
 */

@WithPacketType(PacketType.GET_DATA)
public class Handler_GetData implements Handler {
  public void handle(Server server, Socket socket, Packet packet) throws IOException {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
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
      return;
    }

    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 1);
    if (payload.size() != 1) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);
    final byte[] shard;
    try {
      shard = FileUtils.readFileToByteArray(new File(Const.SHARDS_DIRECTORY + "/" + shardName));
    } catch (IOException e) {
      return;
    }

    new Packet()
      .withType(PacketType.OK)
      .withPayload(shard)
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
