package io.lucky.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;

public class MD5Util {

    /**
     * 普通MD5加密
     *
     * @param plaintext 明文
     * @return 密文
     */
    public static String MD5(String plaintext) {
        if (StringUtils.isEmpty(plaintext)) {
            return null;
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "check jdk";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = plaintext.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 加盐MD5加密
     *
     * @param plaintext
     * @return 密文
     */
    public static String MD5WithSalt(String plaintext) {
        if (StringUtils.isEmpty(plaintext)) {
            return null;
        }
        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        int len = sb.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                sb.append("0");
            }
        }
        String salt = sb.toString();
        plaintext = md5Hex(plaintext + salt);
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = plaintext.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = plaintext.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }

    /**
     * 校验加盐后是否和原文一致
     *
     * @param plaintext
     * @param ciphertext
     * @return
     */
    public static boolean verify(String plaintext, String ciphertext) {
        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = ciphertext.charAt(i);
            cs1[i / 3 * 2 + 1] = ciphertext.charAt(i + 2);
            cs2[i / 3] = ciphertext.charAt(i + 1);
        }
        String salt = new String(cs2);
        return Objects.equals(md5Hex(plaintext + salt), new String(cs1));
    }

    /**
     * 获取十六进制字符串形式的MD5摘要
     */
    private static String md5Hex(String src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(src.getBytes());
            return new String(new Hex().encode(bs));
        } catch (Exception e) {
            return null;
        }
    }
}
