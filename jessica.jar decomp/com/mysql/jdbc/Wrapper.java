/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.sql.SQLException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface Wrapper {
    public <T> T unwrap(Class<T> var1) throws SQLException;

    public boolean isWrapperFor(Class<?> var1) throws SQLException;
}

