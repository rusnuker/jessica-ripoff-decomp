/*
 * Decompiled with CFR 0.152.
 */
package net.optifine.entity.model.anim;

public enum EnumTokenType {
    IDENTIFIER("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", "0123456789_:."),
    CONSTANT("0123456789", "."),
    OPERATOR("+-*/%", 1),
    COMMA(",", 1),
    BRACKET_OPEN("(", 1),
    BRACKET_CLOSE(")", 1);

    private String charsFirst;
    private String charsExt;
    private int maxLen;
    public static final EnumTokenType[] VALUES;

    static {
        VALUES = EnumTokenType.values();
    }

    private EnumTokenType(String charsFirst) {
        this.charsFirst = charsFirst;
        this.charsExt = "";
    }

    private EnumTokenType(String charsFirst, int maxLen) {
        this.charsFirst = charsFirst;
        this.charsExt = "";
        this.maxLen = maxLen;
    }

    private EnumTokenType(String charsFirst, String charsExt) {
        this.charsFirst = charsFirst;
        this.charsExt = charsExt;
    }

    public String getCharsFirst() {
        return this.charsFirst;
    }

    public String getCharsExt() {
        return this.charsExt;
    }

    public static EnumTokenType getTypeByFirstChar(char ch) {
        int i = 0;
        while (i < VALUES.length) {
            EnumTokenType enumtokentype = VALUES[i];
            if (enumtokentype.getCharsFirst().indexOf(ch) >= 0) {
                return enumtokentype;
            }
            ++i;
        }
        return null;
    }

    public boolean hasChar(char ch) {
        if (this.getCharsFirst().indexOf(ch) >= 0) {
            return true;
        }
        return this.getCharsExt().indexOf(ch) >= 0;
    }

    public int getMaxLen() {
        return this.maxLen;
    }
}

