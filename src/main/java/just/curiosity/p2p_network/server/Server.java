package just.curiosity.p2p_network.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import just.curiosity.p2p_network.server.annotation.WithType;
import just.curiosity.p2p_network.server.handler.Handler;
import just.curiosity.p2p_network.server.handler.Handler_AddNode;
import just.curiosity.p2p_network.server.handler.Handler_CloneNodes;
import just.curiosity.p2p_network.server.message.Message;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:06 AM
 */

public class Server {
  private boolean isRunning = true;
  private final int port;
  private final List<Handler> handlers = new ArrayList<>();
  private Set<String> nodes = new HashSet<>();

  {
    handlers.add(new Handler_CloneNodes());
    handlers.add(new Handler_AddNode());
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

  public void setNodes(Set<String> nodes) {
    this.nodes = nodes;
    System.out.println("CLONED NODES: " + nodes);
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

    socket.close();
  }

  public void start() throws IOException {
    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server has been started on port " + port + "..");
      while (isRunning) {
        final Socket socket = serverSocket.accept();
        handleSocket(socket);
      }
    }
  }
}
