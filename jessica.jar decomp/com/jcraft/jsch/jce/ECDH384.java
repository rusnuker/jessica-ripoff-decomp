/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jsch.jce;

import com.jcraft.jsch.ECDH;
import com.jcraft.jsch.jce.ECDHN;

public class ECDH384
extends ECDHN
implements ECDH {
    public void init() throws Exception {
        super.init(384);
    }
}

