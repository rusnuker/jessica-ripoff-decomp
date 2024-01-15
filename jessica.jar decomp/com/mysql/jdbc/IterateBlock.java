/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class IterateBlock<T> {
    DatabaseMetaData.IteratorWithCleanup<T> iteratorWithCleanup;
    Iterator<T> javaIterator;
    boolean stopIterating = false;

    IterateBlock(DatabaseMetaData.IteratorWithCleanup<T> i) {
        this.iteratorWithCleanup = i;
        this.javaIterator = null;
    }

    IterateBlock(Iterator<T> i) {
        this.javaIterator = i;
        this.iteratorWithCleanup = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doForAll() throws SQLException {
        block5: {
            if (this.iteratorWithCleanup != null) {
                try {
                    while (this.iteratorWithCleanup.hasNext()) {
                        this.forEach(this.iteratorWithCleanup.next());
                        if (!this.stopIterating) continue;
                        break;
                    }
                    Object var2_1 = null;
                }
                catch (Throwable throwable) {
                    Object var2_2 = null;
                    this.iteratorWithCleanup.close();
                    throw throwable;
                }
                this.iteratorWithCleanup.close();
                {
                    break block5;
                }
            }
            while (this.javaIterator.hasNext()) {
                this.forEach(this.javaIterator.next());
                if (!this.stopIterating) continue;
                break;
            }
        }
    }

    abstract void forEach(T var1) throws SQLException;

    public final boolean fullIteration() {
        return !this.stopIterating;
    }
}

