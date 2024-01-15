/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.nntp;

public final class NewsgroupInfo {
    public static final int UNKNOWN_POSTING_PERMISSION = 0;
    public static final int MODERATED_POSTING_PERMISSION = 1;
    public static final int PERMITTED_POSTING_PERMISSION = 2;
    public static final int PROHIBITED_POSTING_PERMISSION = 3;
    private String __newsgroup;
    private int __estimatedArticleCount;
    private int __firstArticle;
    private int __lastArticle;
    private int __postingPermission;

    void _setNewsgroup(String newsgroup) {
        this.__newsgroup = newsgroup;
    }

    void _setArticleCount(int count) {
        this.__estimatedArticleCount = count;
    }

    void _setFirstArticle(int first) {
        this.__firstArticle = first;
    }

    void _setLastArticle(int last) {
        this.__lastArticle = last;
    }

    void _setPostingPermission(int permission) {
        this.__postingPermission = permission;
    }

    public String getNewsgroup() {
        return this.__newsgroup;
    }

    public int getArticleCount() {
        return this.__estimatedArticleCount;
    }

    public int getFirstArticle() {
        return this.__firstArticle;
    }

    public int getLastArticle() {
        return this.__lastArticle;
    }

    public int getPostingPermission() {
        return this.__postingPermission;
    }
}

