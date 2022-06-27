package just.curiosity.p2pack.core.message;

/**
 * @author zerdicorp
 * @project p2pack
 * @created 6/27/22 - 10:29 AM
 */

public class Message {
  private MessageType type;
  private int payloadSize;
  private byte[] payload;

  public boolean parse(String data) {
    try {
      final String[] lines = data.split("\n");
      if (lines.length < 2) {
        return false;
      }

      type = MessageType.valueOf(lines[0]);
      payloadSize = Integer.parseInt(lines[1]);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public MessageType type() {
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
