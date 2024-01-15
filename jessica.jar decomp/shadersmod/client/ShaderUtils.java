/*
 * Decompiled with CFR 0.152.
 */
package shadersmod.client;

import optifine.Config;
import shadersmod.client.ShaderOption;
import shadersmod.client.ShaderProfile;

public class ShaderUtils {
    public static ShaderOption getShaderOption(String name, ShaderOption[] opts) {
        if (opts == null) {
            return null;
        }
        int i = 0;
        while (i < opts.length) {
            ShaderOption shaderoption = opts[i];
            if (shaderoption.getName().equals(name)) {
                return shaderoption;
            }
            ++i;
        }
        return null;
    }

    public static ShaderProfile detectProfile(ShaderProfile[] profs, ShaderOption[] opts, boolean def) {
        if (profs == null) {
            return null;
        }
        int i = 0;
        while (i < profs.length) {
            ShaderProfile shaderprofile = profs[i];
            if (ShaderUtils.matchProfile(shaderprofile, opts, def)) {
                return shaderprofile;
            }
            ++i;
        }
        return null;
    }

    public static boolean matchProfile(ShaderProfile prof, ShaderOption[] opts, boolean def) {
        if (prof == null) {
            return false;
        }
        if (opts == null) {
            return false;
        }
        String[] astring = prof.getOptions();
        int i = 0;
        while (i < astring.length) {
            String s2;
            String s1;
            String s = astring[i];
            ShaderOption shaderoption = ShaderUtils.getShaderOption(s, opts);
            if (shaderoption != null && !Config.equals(s1 = def ? shaderoption.getValueDefault() : shaderoption.getValue(), s2 = prof.getValue(s))) {
                return false;
            }
            ++i;
        }
        return true;
    }
}

