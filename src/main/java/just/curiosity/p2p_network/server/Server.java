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
import just.curiosity.p2p_network.server.annotation.WithPacketType;
import just.curiosity.p2p_network.server.handler.Handler;
import just.curiosity.p2p_network.server.handler.Handler_AddNode;
import just.curiosity.p2p_network.server.handler.Handler_CloneNodes;
import just.curiosity.p2p_network.server.handler.Handler_DeleteData;
import just.curiosity.p2p_network.server.handler.Handler_GetData;
import just.curiosity.p2p_network.server.handler.Handler_RenameData;
import just.curiosity.p2p_network.server.handler.Handler_SaveData;
import just.curiosity.p2p_network.server.packet.Packet;
import just.curiosity.p2p_network.server.util.Logger;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:06 AM
 */

public class Server {
  private final boolean isRunning = true;
  private final int port;
  private final List<Handler> handlers = new ArrayList<>();
  private final Set<String> nodes = new HashSet<>();

  {
    handlers.add(new Handler_CloneNodes());
    handlers.add(new Handler_AddNode());
    handlers.add(new Handler_SaveData());
    handlers.add(new Handler_GetData());
    handlers.add(new Handler_DeleteData());
    handlers.add(new Handler_RenameData());
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

    for (Handler handler : handlers) {
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(WithPacketType.class)) {
        final WithPacketType ann = clazz.getAnnotation(WithPacketType.class);
        if (ann.value().equals(packet.type())) {
          handler.handle(this, socket, packet);
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
      while (isRunning) {
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
        System.out.println("CLONED NODES: " + nodes); // TODO: remove debug log
        return;
      }

      nodes.addAll(Arrays.asList(new String(packet.payload(), 0, packet.payloadSize())
        .split(",")));
    }
    System.out.println("CLONED NODES: " + nodes); // TODO: remove debug log
  }
}
