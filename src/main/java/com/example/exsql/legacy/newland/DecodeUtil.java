package com.example.exsql.legacy.newland;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zxy
 * @version 2017/12/11 Description: Modified By:
 */
public class DecodeUtil {
  private static final Logger logger = LoggerFactory.getLogger(DecodeUtil.class);

  // BASE64解密
  public static byte[] decodeBase64(String in) throws DecodeException {
    try {
      return Base64.getDecoder().decode(in);
    } catch (Exception e) {
      logger.error("Base64 decoding failed", e);
      throw new DecodeException("Failed to decode Base64 string", e);
    }
  }

  /** 加密算法,可用 DES,DESede,Blowfish. */
  private static final String ALGORITHM = "DES";

  /**
   * DES解密算法
   *
   * @param data 密码
   * @param cryptKey 密钥 要是偶数
   * @throws Exception
   */
  public static String decrypt(String data, String cryptKey) throws Exception {
    return new String(
        decrypt(hex2byte(data.getBytes("utf-8")), cryptKey.getBytes("utf-8")), "utf-8");
  }

  /**
   * DES加密算法
   *
   * @param data 密码
   * @param cryptKey 密钥 要是偶数
   * @throws Exception
   */
  public static String encrypt(String data, String cryptKey) throws Exception {
    return byte2hex(encrypt(data.getBytes("utf-8"), cryptKey.getBytes("utf-8")));
  }

  private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
    // DES算法要求有一个可信任的随机数源
    SecureRandom sr = new SecureRandom();
    // 从原始密匙数据创建DESKeySpec对象
    DESKeySpec dks = new DESKeySpec(key);
    // 创建一个密匙工厂，然后用它把DESKeySpec转换成
    // 一个SecretKey对象
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
    SecretKey securekey = keyFactory.generateSecret(dks);
    // Cipher对象实际完成加密操作
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    // 用密匙初始化Cipher对象
    cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
    // 现在，获取数据并加密
    // 正式执行加密操作
    return cipher.doFinal(data);
  }

  private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
    // DES算法要求有一个可信任的随机数源
    SecureRandom sr = new SecureRandom();
    // 从原始密匙数据创建一个DESKeySpec对象
    DESKeySpec dks = new DESKeySpec(key);
    // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
    // 一个SecretKey对象
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
    SecretKey securekey = keyFactory.generateSecret(dks);
    // Cipher对象实际完成解密操作
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    // 用密匙初始化Cipher对象
    cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
    // 现在，获取数据并解密
    // 正式执行解密操作
    return cipher.doFinal(data);
  }

  private static byte[] hex2byte(byte[] byteDate) throws UnsupportedEncodingException {
    if ((byteDate.length % 2) != 0) {
      throw new IllegalArgumentException("长度不是偶数");
    }
    byte[] b2 = new byte[byteDate.length / 2];
    for (int n = 0; n < byteDate.length; n += 2) {
      String item = new String(byteDate, n, 2, "utf-8");
      b2[n / 2] = (byte) Integer.parseInt(item, 16);
    }
    return b2;
  }

  private static String byte2hex(byte[] byteDateArray) {
    String hs = "";
    String stmp;
    for (byte byteDate : byteDateArray) {
      stmp = (Integer.toHexString(byteDate & 0XFF));
      if (stmp.length() == 1) {
        hs = hs + "0" + stmp;
      } else {
        hs = hs + stmp;
      }
    }
    return hs.toUpperCase();
  }
}
