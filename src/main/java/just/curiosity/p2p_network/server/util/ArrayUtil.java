package just.curiosity.p2p_network.server.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/3/22 - 2:59 PM
 */

public class ArrayUtil {
  public static List<byte[]> split(byte[] arr, byte value, int limit) {
    final List<byte[]> result = new ArrayList<>();
    int offset = 0;
    for (int i = 0; i < arr.length; i++) {
      if (i == arr.length - 1) {
        result.add(Arrays.copyOfRange(arr, offset, i + 1));
        break;
      }

      if (arr[i] == value) {
        if (limit != -1 && result.size() + 1 == limit) {
          result.add(Arrays.copyOfRange(arr, offset, arr.length));
          break;
        }

        result.add(Arrays.copyOfRange(arr, offset, i));
        offset = i + 1;
      }
    }

    return result;
  }
}
