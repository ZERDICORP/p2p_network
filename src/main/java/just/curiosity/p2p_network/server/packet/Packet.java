package just.curiosity.p2p_network.server.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  public byte[] build() throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(type.toString().getBytes());
    byteArrayOutputStream.write(10); // new line
    byteArrayOutputStream.write(String.valueOf(payload.length).getBytes());
    byteArrayOutputStream.write(10); // new line
    byteArrayOutputStream.write(payload);

    return byteArrayOutputStream.toByteArray();
  }

  public boolean parse(String data) {
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
}
