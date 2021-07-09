package pl.poznan.put.voip.core.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class AesUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Coś sie popsuło przy generowaniu klucza AES", e);
        }
    }

    public static SecretKey getSecretKey(byte[] data) {
        return new SecretKeySpec(data, "AES");
    }

    public static byte[] encrypt(byte[] data, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new RuntimeException("Coś sie popsuło przy szyfrowaniu AES", e);
        }
    }

    public static byte[] decrypt(byte[] data, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new RuntimeException("Coś sie popsuło przy deszyfrowaniu AES", e);
        }
    }

}
