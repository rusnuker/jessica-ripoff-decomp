/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BotUtils {
    public static byte[] createHandshakeMessage(String host, int port) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(buffer);
        handshake.writeByte(0);
        BotUtils.writeVarInt(handshake, 340);
        BotUtils.writeString(handshake, host, StandardCharsets.UTF_8);
        handshake.writeShort(port);
        BotUtils.writeVarInt(handshake, 2);
        return buffer.toByteArray();
    }

    public static byte[] createLogin(String s) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream login = new DataOutputStream(buffer);
        login.writeByte(0);
        BotUtils.writeString(login, s, StandardCharsets.UTF_8);
        return buffer.toByteArray();
    }

    public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        BotUtils.writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        byte k;
        int i = 0;
        int j = 0;
        do {
            k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((k & 0x80) == 128);
        return i;
    }
}

