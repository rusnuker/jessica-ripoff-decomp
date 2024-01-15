/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.net.nntp;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.net.nntp.ThreadContainer;
import org.apache.commons.net.nntp.Threadable;

public class Threader {
    private ThreadContainer root;
    private HashMap idTable;
    private int bogusIdCount = 0;

    public Threadable thread(Threadable[] messages) {
        if (messages == null) {
            return null;
        }
        this.idTable = new HashMap();
        for (int i = 0; i < messages.length; ++i) {
            if (messages[i].isDummy()) continue;
            this.buildContainer(messages[i]);
        }
        this.root = this.findRootSet();
        this.idTable.clear();
        this.idTable = null;
        this.pruneEmptyContainers(this.root);
        this.root.reverseChildren();
        this.gatherSubjects();
        if (this.root.next != null) {
            throw new RuntimeException("root node has a next:" + this.root);
        }
        ThreadContainer r = this.root.child;
        while (r != null) {
            if (r.threadable == null) {
                r.threadable = r.child.threadable.makeDummy();
            }
            r = r.next;
        }
        Threadable result = this.root.child == null ? null : this.root.child.threadable;
        this.root.flush();
        this.root = null;
        return result;
    }

    private void buildContainer(Threadable threadable) {
        String id = threadable.messageThreadId();
        ThreadContainer container = (ThreadContainer)this.idTable.get(id);
        if (container != null) {
            if (container.threadable != null) {
                id = "<Bogus-id:" + this.bogusIdCount++ + ">";
                container = null;
            } else {
                container.threadable = threadable;
            }
        }
        if (container == null) {
            container = new ThreadContainer();
            container.threadable = threadable;
            this.idTable.put(id, container);
        }
        ThreadContainer parentRef = null;
        String[] references = threadable.messageThreadReferences();
        for (int i = 0; i < references.length; ++i) {
            String refString = references[i];
            ThreadContainer ref = (ThreadContainer)this.idTable.get(refString);
            if (ref == null) {
                ref = new ThreadContainer();
                this.idTable.put(refString, ref);
            }
            if (parentRef != null && ref.parent == null && parentRef != ref && !parentRef.findChild(ref)) {
                ref.parent = parentRef;
                ref.next = parentRef.child;
                parentRef.child = ref;
            }
            parentRef = ref;
        }
        if (parentRef != null && (parentRef == container || container.findChild(parentRef))) {
            parentRef = null;
        }
        if (container.parent != null) {
            ThreadContainer prev = null;
            ThreadContainer rest = container.parent.child;
            while (rest != null && rest != container) {
                prev = rest;
                rest = rest.next;
            }
            if (rest == null) {
                throw new RuntimeException("Didnt find " + container + " in parent" + container.parent);
            }
            if (prev == null) {
                container.parent.child = container.next;
            } else {
                prev.next = container.next;
            }
            container.next = null;
            container.parent = null;
        }
        if (parentRef != null) {
            container.parent = parentRef;
            container.next = parentRef.child;
            parentRef.child = container;
        }
    }

    private ThreadContainer findRootSet() {
        ThreadContainer root = new ThreadContainer();
        Iterator iter = this.idTable.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            ThreadContainer c = (ThreadContainer)this.idTable.get(key);
            if (c.parent != null) continue;
            if (c.next != null) {
                throw new RuntimeException("c.next is " + c.next.toString());
            }
            c.next = root.child;
            root.child = c;
        }
        return root;
    }

    private void pruneEmptyContainers(ThreadContainer parent) {
        ThreadContainer prev = null;
        ThreadContainer container = parent.child;
        ThreadContainer next = container.next;
        while (container != null) {
            if (container.threadable == null && container.child == null) {
                if (prev == null) {
                    parent.child = container.next;
                } else {
                    prev.next = container.next;
                }
                container = prev;
            } else if (container.threadable == null && container.child != null && (container.parent != null || container.child.next == null)) {
                ThreadContainer kids = container.child;
                if (prev == null) {
                    parent.child = kids;
                } else {
                    prev.next = kids;
                }
                ThreadContainer tail = kids;
                while (tail.next != null) {
                    tail.parent = container.parent;
                    tail = tail.next;
                }
                tail.parent = container.parent;
                tail.next = container.next;
                next = kids;
                container = prev;
            } else if (container.child != null) {
                this.pruneEmptyContainers(container);
            }
            prev = container;
            container = next;
            next = container == null ? null : container.next;
        }
    }

    private void gatherSubjects() {
        int count = 0;
        ThreadContainer c = this.root.child;
        while (c != null) {
            ++count;
            c = c.next;
        }
        HashMap<String, ThreadContainer> subjectTable = new HashMap<String, ThreadContainer>((int)((double)count * 1.2), 0.9f);
        count = 0;
        ThreadContainer c2 = this.root.child;
        while (c2 != null) {
            ThreadContainer old;
            String subj;
            Threadable threadable = c2.threadable;
            if (threadable == null) {
                threadable = c2.child.threadable;
            }
            if ((subj = threadable.simplifiedSubject()) != null && subj != "" && ((old = (ThreadContainer)subjectTable.get(subj)) == null || c2.threadable == null && old.threadable != null || old.threadable != null && old.threadable.subjectIsReply() && c2.threadable != null && !c2.threadable.subjectIsReply())) {
                subjectTable.put(subj, c2);
                ++count;
            }
            c2 = c2.next;
        }
        if (count == 0) {
            return;
        }
        ThreadContainer prev = null;
        ThreadContainer c3 = this.root.child;
        ThreadContainer rest = c3.next;
        while (c3 != null) {
            ThreadContainer old;
            String subj;
            Threadable threadable = c3.threadable;
            if (threadable == null) {
                threadable = c3.child.threadable;
            }
            if ((subj = threadable.simplifiedSubject()) != null && subj != "" && (old = (ThreadContainer)subjectTable.get(subj)) != c3) {
                if (prev == null) {
                    this.root.child = c3.next;
                } else {
                    prev.next = c3.next;
                }
                c3.next = null;
                if (old.threadable == null && c3.threadable == null) {
                    ThreadContainer tail = old.child;
                    while (tail != null && tail.next != null) {
                        tail = tail.next;
                    }
                    tail.next = c3.child;
                    tail = c3.child;
                    while (tail != null) {
                        tail.parent = old;
                        tail = tail.next;
                    }
                    c3.child = null;
                } else if (old.threadable == null || c3.threadable != null && c3.threadable.subjectIsReply() && !old.threadable.subjectIsReply()) {
                    c3.parent = old;
                    c3.next = old.child;
                    old.child = c3;
                } else {
                    ThreadContainer newc = new ThreadContainer();
                    newc.threadable = old.threadable;
                    ThreadContainer tail = newc.child = old.child;
                    while (tail != null) {
                        tail.parent = newc;
                        tail = tail.next;
                    }
                    old.threadable = null;
                    old.child = null;
                    c3.parent = old;
                    newc.parent = old;
                    old.child = c3;
                    c3.next = newc;
                }
                c3 = prev;
            }
            prev = c3;
            c3 = rest;
            rest = rest == null ? null : rest.next;
        }
        subjectTable.clear();
        subjectTable = null;
    }
}

