/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.nntp;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.commons.net.nntp.Threadable;

public class Article
implements Threadable {
    private int articleNumber;
    private String subject;
    private String date;
    private String articleId;
    private String simplifiedSubject;
    private String from;
    private StringBuffer header = new StringBuffer();
    private StringBuffer references;
    private boolean isReply = false;
    public Article kid;
    public Article next;

    public void addHeaderField(String name, String val) {
        this.header.append(name);
        this.header.append(": ");
        this.header.append(val);
        this.header.append('\n');
    }

    public void addReference(String msgId) {
        if (this.references == null) {
            this.references = new StringBuffer();
            this.references.append("References: ");
        }
        this.references.append(msgId);
        this.references.append("\t");
    }

    public String[] getReferences() {
        if (this.references == null) {
            return new String[0];
        }
        ArrayList<String> list = new ArrayList<String>();
        int terminator = this.references.toString().indexOf(58);
        StringTokenizer st = new StringTokenizer(this.references.substring(terminator), "\t");
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return (String[])list.toArray();
    }

    private void simplifySubject() {
        int start = 0;
        String subject = this.getSubject();
        int len = subject.length();
        boolean done = false;
        while (!done) {
            int end;
            done = true;
            while (start < len && subject.charAt(start) == ' ') {
                ++start;
            }
            if (!(start >= len - 2 || subject.charAt(start) != 'r' && subject.charAt(start) != 'R' || subject.charAt(start + 1) != 'e' && subject.charAt(start + 1) != 'E')) {
                if (subject.charAt(start + 2) == ':') {
                    start += 3;
                    this.isReply = true;
                    done = false;
                } else if (start < len - 2 && (subject.charAt(start + 2) == '[' || subject.charAt(start + 2) == '(')) {
                    int i;
                    for (i = start + 3; i < len && subject.charAt(i) >= '0' && subject.charAt(i) <= '9'; ++i) {
                    }
                    if (i < len - 1 && (subject.charAt(i) == ']' || subject.charAt(i) == ')') && subject.charAt(i + 1) == ':') {
                        start = i + 2;
                        this.isReply = true;
                        done = false;
                    }
                }
            }
            if (this.simplifiedSubject == "(no subject)") {
                this.simplifiedSubject = "";
            }
            for (end = len; end > start && subject.charAt(end - 1) < ' '; --end) {
            }
            if (start == 0 && end == len) {
                this.simplifiedSubject = subject;
                continue;
            }
            this.simplifiedSubject = subject.substring(start, end);
        }
    }

    public static void printThread(Article article, int depth) {
        for (int i = 0; i < depth; ++i) {
            System.out.print("==>");
        }
        System.out.println(article.getSubject() + "\t" + article.getFrom());
        if (article.kid != null) {
            Article.printThread(article.kid, depth + 1);
        }
        if (article.next != null) {
            Article.printThread(article.next, depth);
        }
    }

    public String getArticleId() {
        return this.articleId;
    }

    public int getArticleNumber() {
        return this.articleNumber;
    }

    public String getDate() {
        return this.date;
    }

    public String getFrom() {
        return this.from;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setArticleId(String string) {
        this.articleId = string;
    }

    public void setArticleNumber(int i) {
        this.articleNumber = i;
    }

    public void setDate(String string) {
        this.date = string;
    }

    public void setFrom(String string) {
        this.from = string;
    }

    public void setSubject(String string) {
        this.subject = string;
    }

    public boolean isDummy() {
        return this.getSubject() == null;
    }

    public String messageThreadId() {
        return this.articleId;
    }

    public String[] messageThreadReferences() {
        return this.getReferences();
    }

    public String simplifiedSubject() {
        if (this.simplifiedSubject == null) {
            this.simplifySubject();
        }
        return this.simplifiedSubject;
    }

    public boolean subjectIsReply() {
        if (this.simplifiedSubject == null) {
            this.simplifySubject();
        }
        return this.isReply;
    }

    public void setChild(Threadable child) {
        this.kid = (Article)child;
        this.flushSubjectCache();
    }

    private void flushSubjectCache() {
        this.simplifiedSubject = null;
    }

    public void setNext(Threadable next) {
        this.next = (Article)next;
        this.flushSubjectCache();
    }

    public Threadable makeDummy() {
        return new Article();
    }
}

