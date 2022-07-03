package just.curiosity.p2p_network.server.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/3/22 - 2:59 PM
 */

public class ByteArraySplitter {
  private final List<byte[]> payload = new ArrayList<>();

  public ByteArraySplitter(byte[] arr, byte value, int limit) {
    split(arr, value, limit);
  }

  public void split(byte[] arr, byte value, int limit) {
    int offset = 0;
    for (int i = 0; i < arr.length; i++) {
      if (i == arr.length - 1) {
        payload.add(Arrays.copyOfRange(arr, offset, i + 1));
        break;
      }

      if (arr[i] == value) {
        if (limit != -1 && payload.size() + 1 == limit) {
          payload.add(Arrays.copyOfRange(arr, offset, arr.length));
          break;
        }

        payload.add(Arrays.copyOfRange(arr, offset, i));
        offset = i + 1;
      }
    }
  }

  public byte[] get(int i) {
    return payload.get(i);
  }

  public String getAsString(int i) {
    return new String(payload.get(i));
  }

  public int size() {
    return payload.size();
  }
}
