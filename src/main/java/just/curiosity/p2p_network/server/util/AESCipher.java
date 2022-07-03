package just.curiosity.p2p_network.server.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author zerdicorp
 * @project p2p_network
 * @created 7/3/22 - 1:47 PM
 */

public final class AESCipher {
  private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
    return Cipher.getInstance("AES/ECB/PKCS5Padding");
  }

  private static SecretKeySpec getKeySpec(String keyString) {
    final MessageDigest sha;
    try {
      sha = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    byte[] key = keyString.getBytes(StandardCharsets.UTF_8);
    key = sha.digest(key);
    key = Arrays.copyOf(key, 16);

    return new SecretKeySpec(key, "AES");
  }

  public static byte[] encrypt(byte[] data, String secret) {
    final SecretKeySpec secretKeySpec = getKeySpec(secret);
    try {
      final Cipher cipher = getCipher();
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
      return cipher.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decrypt(byte[] data, String secret) {
    final SecretKeySpec secretKeySpec = getKeySpec(secret);
    try {
      final Cipher cipher = getCipher();
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
      return cipher.doFinal(Base64.getDecoder().decode(data));
    } catch (Exception e) {
      return null;
    }
  }
}
