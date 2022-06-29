package just.curiosity.p2p_network.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.handler.Handler;
import just.curiosity.p2p_network.server.handler.Handler_AddNode;
import just.curiosity.p2p_network.server.handler.Handler_CloneNodes;
import just.curiosity.p2p_network.server.handler.Handler_SaveData;
import just.curiosity.p2p_network.server.message.Message;
import just.curiosity.p2p_network.server.message.MessageType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:06 AM
 */

public class Server {
  private boolean isRunning = true;
  private final int port;
  private final List<String> dataStorage = new ArrayList<>();
  private final List<Handler> handlers = new ArrayList<>();
  private final Set<String> nodes = new HashSet<>();

  {
    handlers.add(new Handler_CloneNodes());
    handlers.add(new Handler_AddNode());
    handlers.add(new Handler_SaveData());
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

  public List<String> dataStorage() {
    return dataStorage;
  }

  public void sendToAll(Message message) {
    nodes.parallelStream()
      .forEach(nodeAddress -> {
        try (final Socket nodeSocket = new Socket(nodeAddress, port)) {
          final OutputStream outputStream = nodeSocket.getOutputStream();
          outputStream.write(message.build());
        } catch (IOException e) {
          System.out.println("Can't send message to address \"" + nodeAddress + "\".. " + e);
        }
      });
  }

  private int headerSize(byte[] buffer, int size) {
    int count = 0;
    for (int i = 0; i < size; ++i)
      if (buffer[i] == '\n') {
        count++;
        if (count == 2) {
          return i;
        }
      }
    return buffer.length;
  }

  private void handleSocket(Socket socket) throws IOException {
    final InputStream inputStream = socket.getInputStream();

    final byte[] firstSegmentBuffer = new byte[1024];
    byte[] payloadBuffer;

    final int firstSegmentSize = inputStream.read(firstSegmentBuffer);
    if (firstSegmentSize == -1) {
      return;
    }

    final int headerSize = headerSize(firstSegmentBuffer, firstSegmentSize);

    Message message = new Message();
    if (!message.parse(new String(firstSegmentBuffer, 0, headerSize, StandardCharsets.UTF_8))) {
      return;
    }

    if (message.payloadSize() > 0) {
      payloadBuffer = new byte[message.payloadSize()];

      int payloadBytesLengthInFirstSegment = firstSegmentSize - (headerSize + 1);
      for (int i = 0; i < payloadBytesLengthInFirstSegment && i < payloadBuffer.length; ++i) {
        payloadBuffer[i] = firstSegmentBuffer[i + (headerSize + 1)];
      }

      int offset = payloadBytesLengthInFirstSegment;
      int segmentSize;

      while (offset < payloadBuffer.length &&
        (segmentSize = inputStream.read(payloadBuffer, offset, payloadBuffer.length - offset)) > 0) {
        offset += segmentSize;
      }

      message.payload(payloadBuffer);
    }

    for (Handler handler : handlers) {
      final Class<?> clazz = handler.getClass();
      if (clazz.isAnnotationPresent(WithType.class)) {
        final WithType ann = clazz.getAnnotation(WithType.class);
        if (ann.value().equals(message.type())) {
          handler.handle(this, socket, message);
          break;
        }
      } else {
        System.out.println("Handler \"" + clazz.getName() + "\" have no \"" +
          WithType.class.getName() + "\" annotation.. ignore");
      }
    }
  }

  public void cloneNodes(String rootNodeAddress) throws IOException {
    nodes.add(rootNodeAddress);
    try (final Socket socket = new Socket(rootNodeAddress, port)) {
      socket.getOutputStream().write(new Message(MessageType.CLONE_NODES).build());

      final byte[] buffer = new byte[1024];
      final int size = socket.getInputStream().read(buffer);
      if (size == -1) {
        System.out.println("CLONED NODES: " + nodes); // TODO: remove debug log
        return;
      }

      nodes.addAll(Arrays.asList(new String(buffer, 0, size, StandardCharsets.UTF_8).split(",")));
    }
    System.out.println("CLONED NODES: " + nodes); // TODO: remove debug log
  }

  public void start() throws IOException {
    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server has been started on port " + port + "..");
      while (isRunning) {
        try (final Socket socket = serverSocket.accept()) {
          handleSocket(socket);
        }
      }
    }
  }
}
