/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import java.util.EventListener;

public class RotationUtils
implements EventListener {
    public static float yaw = 0.0f;
    public static float pitch = 0.0f;
    public static float oldYaw = 0.0f;
    public static float oldPitch = 0.0f;

    public static void set(float _yaw, float _pitch) {
        if (!Double.isNaN(_yaw) && !Double.isNaN(_pitch)) {
            while (_pitch < -90.0f) {
                _pitch += 180.0f;
            }
            while (_pitch > 90.0f) {
                _pitch -= 180.0f;
            }
            yaw = _yaw;
            pitch = _pitch;
        }
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getLastYaw() {
        return oldYaw;
    }

    public static float getPitch() {
        return pitch;
    }

    public static float getLastPitch() {
        return oldPitch;
    }
}

