/*
 * Decompiled with CFR 0.152.
 */
package shadersmod.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import shadersmod.client.EnumShaderOption;
import shadersmod.client.GuiShaders;
import shadersmod.client.Shaders;

public class GuiButtonEnumShaderOption
extends GuiButton {
    private EnumShaderOption enumShaderOption = null;

    public GuiButtonEnumShaderOption(EnumShaderOption enumShaderOption, int x, int y, int widthIn, int heightIn) {
        super(enumShaderOption.ordinal(), x, y, widthIn, heightIn, GuiButtonEnumShaderOption.getButtonText(enumShaderOption));
        this.enumShaderOption = enumShaderOption;
    }

    public EnumShaderOption getEnumShaderOption() {
        return this.enumShaderOption;
    }

    private static String getButtonText(EnumShaderOption eso) {
        String s = String.valueOf(I18n.format(eso.getResourceKey(), new Object[0])) + ": ";
        switch (eso) {
            case ANTIALIASING: {
                return String.valueOf(s) + GuiShaders.toStringAa(Shaders.configAntialiasingLevel);
            }
            case NORMAL_MAP: {
                return String.valueOf(s) + GuiShaders.toStringOnOff(Shaders.configNormalMap);
            }
            case SPECULAR_MAP: {
                return String.valueOf(s) + GuiShaders.toStringOnOff(Shaders.configSpecularMap);
            }
            case RENDER_RES_MUL: {
                return String.valueOf(s) + GuiShaders.toStringQuality(Shaders.configRenderResMul);
            }
            case SHADOW_RES_MUL: {
                return String.valueOf(s) + GuiShaders.toStringQuality(Shaders.configShadowResMul);
            }
            case HAND_DEPTH_MUL: {
                return String.valueOf(s) + GuiShaders.toStringHandDepth(Shaders.configHandDepthMul);
            }
            case CLOUD_SHADOW: {
                return String.valueOf(s) + GuiShaders.toStringOnOff(Shaders.configCloudShadow);
            }
            case OLD_HAND_LIGHT: {
                return String.valueOf(s) + Shaders.configOldHandLight.getUserValue();
            }
            case OLD_LIGHTING: {
                return String.valueOf(s) + Shaders.configOldLighting.getUserValue();
            }
            case SHADOW_CLIP_FRUSTRUM: {
                return String.valueOf(s) + GuiShaders.toStringOnOff(Shaders.configShadowClipFrustrum);
            }
            case TWEAK_BLOCK_DAMAGE: {
                return String.valueOf(s) + GuiShaders.toStringOnOff(Shaders.configTweakBlockDamage);
            }
        }
        return String.valueOf(s) + Shaders.getEnumShaderOption(eso);
    }

    public void updateButtonText() {
        this.displayString = GuiButtonEnumShaderOption.getButtonText(this.enumShaderOption);
    }
}

