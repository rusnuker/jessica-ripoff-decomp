/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.net.ssl.HttpsURLConnection;

public class OtherUtils {
    public static boolean isUniqueName(String name) {
        return name.length() > 5 && OtherUtils.isPremiumName(name);
    }

    public static boolean isPremiumName(String name) {
        try {
            URL obj = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            if (con.getResponseCode() != 200) {
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String computeHash(String password, String salt) throws Exception {
        return OtherUtils.sha256(String.valueOf(OtherUtils.sha256(password)) + salt);
    }

    public static boolean comparePassword(String password, String hash) throws Exception {
        String[] line = hash.split("\\$");
        return line[3].equalsIgnoreCase(OtherUtils.computeHash(password, line[2]));
    }

    public static String sha256(String message) throws Exception {
        return OtherUtils.hash(message);
    }

    public static String hash(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return OtherUtils.bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        int i = 0;
        while (i < hash.length) {
            String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
            ++i;
        }
        return hexString.toString();
    }
}

