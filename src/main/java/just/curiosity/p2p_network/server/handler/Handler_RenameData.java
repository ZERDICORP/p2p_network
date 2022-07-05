package just.curiosity.p2p_network.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import just.curiosity.p2p_network.constants.Const;
import just.curiosity.p2p_network.server.Server;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.server.packet.PacketType;
import just.curiosity.p2p_network.server.util.AESCipher;
import just.curiosity.p2p_network.server.util.ByteArraySplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/4/22 - 9:55 AM
 */

@WithPacketType(PacketType.RENAME_DATA)
public class Handler_RenameData implements Handler {
  public void handle(Server server, Socket socket, Packet packet) {
    final String socketAddress = socket.getInetAddress().toString().split("/")[1];

    // If the request came from the local host, then you need
    // to go through the list of nodes and request data from
    // them using the identifier sent by the client.
    if (socketAddress.equals("127.0.0.1")) {
      final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 3);
      if (payload.size() != 3) {
        return;
      }

      final String fileNameHash = DigestUtils.sha256Hex(payload.get(1));
      final String newFileNameHash = DigestUtils.sha256Hex(payload.get(2));
      final File sharedFile = new File(Const.sharedDirectory + "/" + fileNameHash);
      final File newSharedFile = new File(Const.sharedDirectory + "/" + newFileNameHash);
      final Set<String> nodes = server.nodes();
      final String[] shards;
      try {
        final String sharedContent = FileUtils.readFileToString(sharedFile, StandardCharsets.UTF_8);
        shards = sharedContent.split("\n");
      } catch (IOException e) {
        System.out.println("Can't read shared \"" + fileNameHash + "\".. " + e);
        return;
      }

      for (String shardInfo : shards) {
        final String[] shardInfoArr = shardInfo.split(",");
        final String shardName = DigestUtils.sha256Hex(fileNameHash + shardInfoArr[0]);
        final String newShardName = DigestUtils.sha256Hex(newFileNameHash + shardInfoArr[0]);

        System.out.println("RENAMING SHARD: " + shardName + " -> " + newShardName); // TODO: remove debug log

        for (String nodeAddress : nodes) {
          try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
            nodeSocket.getOutputStream()
              .write(new Packet(PacketType.GET_DATA, shardName.getBytes()).build());

            final byte[] buffer = new byte[1024]; // TODO: replace fixed buffer size
            final int size = nodeSocket.getInputStream().read(buffer);
            if (size == -1) {
              continue;
            }

            final byte[] foundShard = Arrays.copyOfRange(buffer, 0, size);
            if (!shardInfoArr[1].equals(DigestUtils.sha256Hex(foundShard))) {
              continue;
            }

            final byte[] decryptedFoundShard = AESCipher.decrypt(foundShard, payload.get(0));
            if (decryptedFoundShard == null) {
              return;
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          try (final Socket nodeSocket = new Socket(nodeAddress, server.port())) {
            nodeSocket.getOutputStream()
              .write(new Packet(PacketType.RENAME_DATA, (shardName + "\n" + newShardName).getBytes()).build());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      try {
        FileUtils.moveFile(sharedFile, newSharedFile);
      } catch (IOException e) {
        System.out.println("Can't rename shared \"" + fileNameHash + "\".. " + e);
      }
      return;
    }

    final ByteArraySplitter payload = new ByteArraySplitter(packet.payload(), (byte) '\n', 2);
    if (payload.size() != 2) {
      return;
    }

    final String shardName = DigestUtils.sha256Hex(payload.getAsString(0) + socketAddress);
    final String newShardName = DigestUtils.sha256Hex(payload.getAsString(1) + socketAddress);
    try {
      FileUtils.moveFile(new File(Const.shardsDirectory + "/" + shardName),
        new File(Const.shardsDirectory + "/" + newShardName));
      System.out.println("RENAMED SHARD: " + shardName + " -> " + newShardName); // TODO: remove debug log
    } catch (IOException e) {
      System.out.println("Can't rename shard \"" + shardName + "\".. " + e);
    }
  }
}
