package pl.poznan.put.voip.core.utils;

import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptUtils {

    private static final SecureRandom random = new SecureRandom();

    public static IvParameterSpec generateIV() {
        byte[] ivBytes = new byte[16];
        random.nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }

    public static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}
