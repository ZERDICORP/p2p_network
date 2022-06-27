package just.curiosity.p2pack.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import just.curiosity.p2pack.core.handler.MessageHandler__Clone;
import just.curiosity.p2pack.core.message.Message;

/**
 * @author zerdicorp
 * @project p2pack
 * @created 6/27/22 - 10:06 AM
 */

public class Server {
  private final boolean isRunning = true;
  private final int port;
  private final List<MessageHandler__Clone> handlers = new ArrayList<>();
  private final Set<String> nodes = new HashSet<>();

  {
    handlers.add(new MessageHandler__Clone());
  }

  public Server(int port) {
    this.port = port;
  }

  public Set<String> nodes() {
    return nodes;
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

    for (MessageHandler__Clone handler : handlers) {
      if (handler.handle(this, socket, message)) {
        break;
      }
    }

    socket.close();
  }

  public void start() throws IOException {
    try (final ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server has been started on port " + port + "..");
      while (isRunning) {
        final Socket socket = serverSocket.accept();
        System.out.println("CONNECTED: " + socket.getInetAddress().toString());
        handleSocket(socket);
      }
    }
  }
}
