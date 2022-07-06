package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
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
  public void handle(Server server, Socket socket, Packet packet) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
      final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
      if (payload.size() != 2) {
        return;
      }

      final String fileName = DigestUtils.sha256Hex(payload.get(1));
      final Set<String> nodes = server.nodes();
      final String[] shards;
      try {
        final String sharedContent = FileUtils.readFileToString(
          new File(Const.META_DIRECTORY + "/" + fileName), StandardCharsets.UTF_8);
        shards = sharedContent.split("\n");
      } catch (IOException e) {
        new Packet()
          .withType(PacketType.FILE_NOT_FOUND)
          .sendTo(socket);
        return;
      }

      final StringBuilder originalFileContent = new StringBuilder();
      for (String shardInfo : shards) {
        final String[] shardInfoArr = shardInfo.split(",");
        final String shardName = DigestUtils.sha256Hex(fileName + shardInfoArr[0]);

        System.out.println("REQUESTING SHARD: " + shardName); // TODO: remove debug log

        String shard = null;
        for (String nodeAddress : nodes) {
          try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
            new Packet()
              .withType(PacketType.GET_DATA)
              .withPayload(shardName.getBytes())
              .sendTo(nodeSocket);

            final Packet getShardPacket = Packet.read(nodeSocket.getInputStream());
            if (getShardPacket == null) {
              continue;
            }

            final byte[] foundShard = getShardPacket.payload();
            // If the hash of the found shard does not match the
            // signature, then the shard has been modified and is
            // no longer valid. Skip and continue the search.
            if (!shardInfoArr[1].equals(DigestUtils.sha256Hex(foundShard))) {
              continue;
            }

            final byte[] decryptedFoundShard = AESCipher.decrypt(foundShard, payload.get(0));
            // If the decrypt method returns null, then the wrong
            // key was used.
            if (decryptedFoundShard == null) {
              return;
            }

            shard = new String(decryptedFoundShard);
            break;
          } catch (IOException e) {
            throw new RuntimeException(e); // TODO: replace exception with log
          }
        }

        if (shard == null) {
          return;
        }

        originalFileContent.append(shard);
      }

      new Packet()
        .withType(PacketType.OK)
        .withPayload(originalFileContent.toString().getBytes())
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
      System.out.println("Can't read shard \"" + shardName + "\".. " + e);
      return;
    }

    new Packet()
      .withType(PacketType.OK)
      .withPayload(shard)
      .sendTo(socket);
  }
}
