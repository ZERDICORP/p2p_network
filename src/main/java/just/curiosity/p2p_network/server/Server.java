package just.curiosity.p2p_network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import just.curiosity.p2p_network.constants.LogMsg;
import just.curiosity.p2p_network.constants.PacketType;
import just.curiosity.p2p_network.packet.Packet;
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.annotation.WithSocketAddress;
import just.curiosity.p2p_network.server.handler.Handler;
import just.curiosity.p2p_network.server.handler.Handler_AddNode;
import just.curiosity.p2p_network.server.handler.Handler_CloneNodes;
import just.curiosity.p2p_network.server.handler.Handler_DeleteFile;
import just.curiosity.p2p_network.server.handler.Handler_DeleteShard;
import just.curiosity.p2p_network.server.handler.Handler_GetFile;
import just.curiosity.p2p_network.server.handler.Handler_GetShard;
import just.curiosity.p2p_network.server.handler.Handler_RenameFile;
import just.curiosity.p2p_network.server.handler.Handler_RenameShard;
import just.curiosity.p2p_network.server.handler.Handler_SaveFile;
import just.curiosity.p2p_network.server.handler.Handler_SaveShard;
import just.curiosity.p2p_network.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:06 AM
 */

public class Server {
  private final int port;
  private final List<Handler> handlers = new ArrayList<>();
  private final Set<String> nodes = new HashSet<>();

  {
    handlers.add(new Handler_CloneNodes());
    handlers.add(new Handler_AddNode());
    handlers.add(new Handler_SaveShard());
    handlers.add(new Handler_SaveFile());
    handlers.add(new Handler_GetShard());
    handlers.add(new Handler_GetFile());
    handlers.add(new Handler_DeleteShard());
    handlers.add(new Handler_DeleteFile());
    handlers.add(new Handler_RenameShard());
    handlers.add(new Handler_RenameFile());
  }

  public Server(int port) {
    this.port = port;
  }

  public int port() {
    return port;
  }

  public Set<String> nodes() {
    return nodes;
  }

  public void sendToAll(Packet packet) {
    nodes.parallelStream()
      .forEach(nodeAddress -> {
        try (final Socket socket = new Socket(nodeAddress, port)) {
          packet.sendTo(socket);
        } catch (IOException e) {
          Logger.log(LogMsg.CANT_CONNECT_TO_PEER, new String[]{
            nodeAddress,
            e.getMessage()});
        }
      });
  }

  private void handleSocket(Socket socket) throws IOException {
    final Packet packet = Packet.read(socket.getInputStream());
    if (packet == null) {
      return;
    }

    final String socketAddress = socket.getInetAddress().toString().split("/")[1];
    for (Handler handler : handlers) {
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(WithPacketType.class)) {
        final WithPacketType withPacketTypeAnn = clazz.getAnnotation(WithPacketType.class);
        if (withPacketTypeAnn.value().equals(packet.type())) {
          if (clazz.isAnnotationPresent(WithSocketAddress.class)) {
            final WithSocketAddress withSocketAddressAnn = clazz.getAnnotation(WithSocketAddress.class);
            if (!withSocketAddressAnn.value().equals(socketAddress)) {
              continue;
            }
          }
          handler.handle(this, socket, socketAddress, packet);
          break;
        }
      } else {
        Logger.log(LogMsg.HANDLER_HAS_NO_ANNOTATION, new String[]{
          clazz.getName(),
          WithPacketType.class.getName()});
      }
    }
  }

  public void start() throws IOException {
    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      Logger.log(LogMsg.SERVER_STARTED, String.valueOf(port));
      while (true) {
        try (final Socket socket = serverSocket.accept()) {
          handleSocket(socket);
        } catch (IOException e) {
          Logger.log(LogMsg.SOCKET_HANDLING_ERROR, e.getMessage());
        }
      }
    }
  }

  public void cloneNodes(String rootNodeAddress) throws IOException {
    nodes.add(rootNodeAddress);
    try (final Socket socket = new Socket(rootNodeAddress, port)) {
      new Packet()
        .withType(PacketType.CLONE_NODES)
        .sendTo(socket);

      final Packet packet = Packet.read(socket.getInputStream());
      if (packet == null || packet.payloadSize() == 0) {
        return;
      }

      nodes.addAll(Arrays.asList(new String(packet.payload(), 0, packet.payloadSize())
        .split(",")));
    }
  }
}
