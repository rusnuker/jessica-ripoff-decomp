/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.ResourceLeakDetector
 *  io.netty.util.ResourceLeakDetector$Level
 */
package net.minecraft.util;

import io.netty.util.ResourceLeakDetector;

public class ChatAllowedCharacters {
    public static final ResourceLeakDetector.Level NETTY_LEAK_DETECTION = ResourceLeakDetector.Level.DISABLED;
    public static final char[] ILLEGAL_STRUCTURE_CHARACTERS;
    public static final char[] ILLEGAL_FILE_CHARACTERS;

    static {
        char[] cArray = new char[14];
        cArray[0] = 46;
        cArray[1] = 10;
        cArray[2] = 13;
        cArray[3] = 9;
        cArray[5] = 12;
        cArray[6] = 96;
        cArray[7] = 63;
        cArray[8] = 42;
        cArray[9] = 92;
        cArray[10] = 60;
        cArray[11] = 62;
        cArray[12] = 124;
        cArray[13] = 34;
        ILLEGAL_STRUCTURE_CHARACTERS = cArray;
        char[] cArray2 = new char[15];
        cArray2[0] = 47;
        cArray2[1] = 10;
        cArray2[2] = 13;
        cArray2[3] = 9;
        cArray2[5] = 12;
        cArray2[6] = 96;
        cArray2[7] = 63;
        cArray2[8] = 42;
        cArray2[9] = 92;
        cArray2[10] = 60;
        cArray2[11] = 62;
        cArray2[12] = 124;
        cArray2[13] = 34;
        cArray2[14] = 58;
        ILLEGAL_FILE_CHARACTERS = cArray2;
        ResourceLeakDetector.setLevel((ResourceLeakDetector.Level)NETTY_LEAK_DETECTION);
    }

    public static boolean isAllowedCharacter(char character) {
        return character != '\u00a7' && character >= ' ' && character != '\u007f';
    }

    public static String filterAllowedCharacters(String input) {
        StringBuilder stringbuilder = new StringBuilder();
        char[] cArray = input.toCharArray();
        int n = cArray.length;
        int n2 = 0;
        while (n2 < n) {
            char c0 = cArray[n2];
            if (ChatAllowedCharacters.isAllowedCharacter(c0)) {
                stringbuilder.append(c0);
            }
            ++n2;
        }
        return stringbuilder.toString();
    }
}

