package com.javath.util;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
        /**
         * Turns array of bytes into string
         * 
         * @param buf
         *                      Array of bytes to convert to hex string
         * @return Generated hex string
         */
        public static String asHex(byte buf[]) {
                StringBuffer strbuf = new StringBuffer(buf.length * 2);
                int i;

                for (i = 0; i < buf.length; i++) {
                        if (((int) buf[i] & 0xff) < 0x10)
                                strbuf.append("0");

                        strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
                }

                return strbuf.toString();
        }

        public static byte[] hexStringToByteArray(String s) {
                int len = s.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                                        .digit(s.charAt(i + 1), 16));
                }
                return data;
        }

        public static String stringToHex(String base) {
                StringBuffer buffer = new StringBuffer();
                int intValue;
                for (int x = 0; x < base.length(); x++) {
                        int cursor = 0;
                        intValue = base.charAt(x);
                        String binaryChar = new String(Integer.toBinaryString(base
                                        .charAt(x)));
                        for (int i = 0; i < binaryChar.length(); i++) {
                                if (binaryChar.charAt(i) == '1') {
                                        cursor += 1;
                                }
                        }
                        if ((cursor % 2) > 0) {
                                intValue += 128;
                        }
                        buffer.append(Integer.toHexString(intValue));
                }
                return buffer.toString();
        }

        public static void main(String[] args) throws Exception {

                String plainText = null;
                String key = null;
                
                if (args.length == 2) {
                        plainText = args[0];
                        key = args[1];
                } else {
                        System.err.println("params text key");
                        System.exit(1);
                }

                if (key.length() != 16) {
                        System.err.println("key length must be 16 characters.");
                        System.exit(1);
                }

                System.out.println("Key: " + key);
                System.out.println("Key (HEX): " + stringToHex(key));
                
                byte[] raw = hexStringToByteArray(stringToHex(key));

                SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

                // Instantiate the cipher
                Cipher cipher = Cipher.getInstance("AES");

                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

                System.out.println("Key (Byte to Hex): " +asHex(raw));

                byte[] encrypted = cipher
                                .doFinal((args.length == 0 ? "This is just an example"
                                                : args[0]).getBytes());
                System.out.println("encrypted string: " + asHex(encrypted));

                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
                byte[] original = cipher.doFinal(encrypted);
                String originalString = new String(original);
                System.out.println("Original string: " + originalString);
        }

}