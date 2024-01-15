/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Extension;
import java.sql.SQLException;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface AuthenticationPlugin
extends Extension {
    public String getProtocolPluginName();

    public boolean requiresConfidentiality();

    public boolean isReusable();

    public void setAuthenticationParameters(String var1, String var2);

    public boolean nextAuthenticationStep(Buffer var1, List<Buffer> var2) throws SQLException;
}

