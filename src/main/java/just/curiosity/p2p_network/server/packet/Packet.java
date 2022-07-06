package just.curiosity.p2p_network.server.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import just.curiosity.p2p_network.constants.PacketType;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 6/27/22 - 10:29 AM
 */

public class Packet {
  private PacketType type;
  private int payloadSize;
  private byte[] payload = new byte[0];

  public Packet() {
  }

  public Packet(PacketType type) {
    this.type = type;
  }

  public Packet(PacketType type, byte[] payload) {
    this.type = type;
    this.payload = payload;
  }

  public PacketType type() {
    return type;
  }

  public byte[] payload() {
    return payload;
  }

  public int payloadSize() {
    return payloadSize;
  }

  public void payload(byte[] payload) {
    this.payload = payload;
  }

  public byte[] build() throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(type.toString().getBytes());
    byteArrayOutputStream.write(10); // new line
    byteArrayOutputStream.write(String.valueOf(payload.length).getBytes());
    byteArrayOutputStream.write(10); // new line
    byteArrayOutputStream.write(payload);

    return byteArrayOutputStream.toByteArray();
  }

  private static int metaSize(byte[] raw, int size) {
    int count = 0;
    for (int i = 0; i < size; ++i)
      if (raw[i] == '\n') {
        count++;
        if (count == 2) {
          return i;
        }
      }
    return raw.length;
  }

  public boolean parseMeta(String data) {
    try {
      final String[] lines = data.split("\n");
      if (lines.length < 2) {
        return false;
      }

      type = PacketType.valueOf(lines[0]);
      payloadSize = Integer.parseInt(lines[1]);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static Packet read(InputStream inputStream) throws IOException {
    final byte[] firstSegmentBuffer = new byte[1024];

    final int firstSegmentSize = inputStream.read(firstSegmentBuffer);
    if (firstSegmentSize == -1) {
      return null;
    }

    final int metaSize = metaSize(firstSegmentBuffer, firstSegmentSize);

    final Packet packet = new Packet();
    if (!packet.parseMeta(new String(firstSegmentBuffer, 0, metaSize))) {
      return null;
    }

    if (packet.payloadSize() > 0) {
      byte[] payloadBuffer = new byte[packet.payloadSize()];

      int payloadBytesLengthInFirstSegment = firstSegmentSize - (metaSize + 1);
      for (int i = 0; i < payloadBytesLengthInFirstSegment && i < payloadBuffer.length; ++i) {
        payloadBuffer[i] = firstSegmentBuffer[i + (metaSize + 1)];
      }

      int offset = payloadBytesLengthInFirstSegment;
      int segmentSize;

      while (offset < payloadBuffer.length &&
        (segmentSize = inputStream.read(payloadBuffer, offset, payloadBuffer.length - offset)) > 0) {
        offset += segmentSize;
      }

      packet.payload(payloadBuffer);
    }

    return packet;
  }
}
