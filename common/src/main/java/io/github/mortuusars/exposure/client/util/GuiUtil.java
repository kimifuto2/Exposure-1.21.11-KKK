package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class GuiUtil {
    public static void blit(GuiGraphics guiGraphics, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(guiGraphics, null, rect, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(GuiGraphics guiGraphics, @Nullable Identifier texture, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(guiGraphics, texture, rect.x, rect.y, rect.width, rect.height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(GuiGraphics guiGraphics, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        blit(guiGraphics, null, x, y, width, height, u, v, textureWidth, textureHeight, zOffset);
    }

    public static void blit(GuiGraphics guiGraphics, @Nullable Identifier texture, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight, float zOffset) {
        if (texture != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int)x, (int)y, u, v, (int)width, (int)height, textureWidth, textureHeight);
        }
    }

    // --

    public static void drawRect(GuiGraphics guiGraphics, Rect2f rect, int color) {
        drawRect(guiGraphics, rect.x, rect.y, rect.width, rect.height, color);
    }

    public static void drawRect(GuiGraphics guiGraphics, float x, float y, float width, float height, int color) {
        guiGraphics.fill((int)x, (int)y, (int)(x + width), (int)(y + height), color);
    }

    // --

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width, int color) {
        renderScrollingString(guiGraphics, font, text, x, y, x + width, y + font.lineHeight, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int fontWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int width = maxX - minX;
        if (fontWidth > width) {
            int remaining = fontWidth - width;
            double d = (double) Util.getMillis() / 400;
            double e = Math.max((double)remaining * 0.5, 3.0);
            double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, remaining);
            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX - (int)g, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int l = Mth.clamp(centerX, minX + fontWidth / 2, maxX - fontWidth / 2);
            guiGraphics.drawCenteredString(font, text, l, y, color);
        }
    }
}
