/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "com.mysql.jdbc.LocalizedErrorMessages";
    private static final ResourceBundle RESOURCE_BUNDLE;

    public static String getString(String key) {
        if (RESOURCE_BUNDLE == null) {
            throw new RuntimeException("Localized messages from resource bundle 'com.mysql.jdbc.LocalizedErrorMessages' not loaded during initialization of driver.");
        }
        try {
            if (key == null) {
                throw new IllegalArgumentException("Message key can not be null");
            }
            String message = RESOURCE_BUNDLE.getString(key);
            if (message == null) {
                message = "Missing error message for key '" + key + "'";
            }
            return message;
        }
        catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object[] args) {
        return MessageFormat.format(Messages.getString(key), args);
    }

    private Messages() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        ResourceBundle temp = null;
        try {
            try {
                temp = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), Messages.class.getClassLoader());
            }
            catch (Throwable t) {
                try {
                    temp = ResourceBundle.getBundle(BUNDLE_NAME);
                }
                catch (Throwable t2) {
                    RuntimeException rt = new RuntimeException("Can't load resource bundle due to underlying exception " + t.toString());
                    rt.initCause(t2);
                    throw rt;
                }
                Object var5_2 = null;
                RESOURCE_BUNDLE = temp;
            }
            Object var5_1 = null;
            RESOURCE_BUNDLE = temp;
        }
        catch (Throwable throwable) {
            Object var5_3 = null;
            RESOURCE_BUNDLE = temp;
            throw throwable;
        }
    }
}

