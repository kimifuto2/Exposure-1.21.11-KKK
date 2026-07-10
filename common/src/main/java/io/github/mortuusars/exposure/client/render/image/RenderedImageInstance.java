package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

public class RenderedImageInstance implements AutoCloseable {
    protected final Identifier initialTextureLocation;
    protected Identifier textureLocation;
    protected RenderableImage image;
    protected DynamicTexture texture;
    protected boolean requiresUpload = true;

    RenderedImageInstance(RenderableImage image) {
        this.image = image;
        this.initialTextureLocation = image.getIdentifier().toIdentifier();
        this.textureLocation = this.initialTextureLocation;
        this.texture = new DynamicTexture(this.textureLocation.toString(), image.width(), image.height(), true);
        Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);

        forceUpload();
    }

    public void replaceData(RenderableImage image) {
        boolean hasChanged = !image.getIdentifier().equals(this.image.getIdentifier());
        this.image = image;
        if (hasChanged) {
            Minecraft.getInstance().getTextureManager().release(this.textureLocation);
            this.texture.close();
            this.texture = new DynamicTexture(image.getIdentifier().toIdentifier().toString(), image.width(), image.height(), true);
            this.textureLocation = image.getIdentifier().toIdentifier();
            Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);
            forceUpload();
        }
    }

    public void forceUpload() {
        this.requiresUpload = true;
    }

    public void ensureUploaded() {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }
    }

    protected void updateTexture() {
        if (texture.getPixels() == null) {
            Exposure.LOGGER.warn("[Exposure] DynamicTexture pixels are null, cannot upload");
            return;
        }
        int width = this.image.width();
        int height = this.image.height();
        Exposure.LOGGER.info("[Exposure] Uploading texture {}x{}", width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ARGB = this.image.getPixelARGB(x, y);
                this.texture.getPixels().setPixel(x, y, Color.ARGBtoABGR(ARGB));
            }
        }
        this.texture.upload();
    }

    public void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        draw(new Matrix4f(poseStack.last().pose()), bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    public void draw(Matrix3x2fStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        draw(matrix3x2fToMatrix4f(poseStack), bufferSource, minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    private static Matrix4f matrix3x2fToMatrix4f(Matrix3x2fStack poseStack) {
        float[] m = new float[6];
        poseStack.get(m);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00(m[0]); matrix4f.m01(m[1]); matrix4f.m02(0); matrix4f.m03(0);
        matrix4f.m10(m[2]); matrix4f.m11(m[3]); matrix4f.m12(0); matrix4f.m13(0);
        matrix4f.m20(0);    matrix4f.m21(0);    matrix4f.m22(1); matrix4f.m23(0);
        matrix4f.m30(m[4]); matrix4f.m31(m[5]); matrix4f.m32(0); matrix4f.m33(1);
        return matrix4f;
    }

    private void draw(Matrix4f matrix4f, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        // Use RenderType.text() which maps to gbuffers_textured in Iris (simple lighting, no PBR/SSR).
        // RenderTypes.entityCutout() maps to gbuffers_entities which applies full PBR + SSR → sky reflections.
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.text(textureLocation));
        vertexConsumer.addVertex(matrix4f, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        vertexConsumer.addVertex(matrix4f, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }

    public void draw(PoseStack poseStack, SubmitNodeCollector nodeCollector, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        // Use RenderTypes.text() to map to gbuffers_textured in Iris shaders,
        // which writes to colortex13 (translucent) with simple lighting - no PBR, no SSR reflections.
        // RenderTypes.entityCutout() maps to gbuffers_entities which applies full PBR + SSR → sky reflections on photos.
        // Vertex format: Position + Color + UV0 + UV2(lightmap) only - no Overlay, no Normal.
        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(textureLocation), (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(pose, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(pose, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
            vertexConsumer.addVertex(pose, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
        });
    }

    public void draw(GuiGraphics guiGraphics, float x, float y, float width, float height, float alpha, Object color) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureLocation, (int)x, (int)y, 0f, 0f, (int)width, (int)height, (int)width, (int)height);
    }

    public void close() {
        Minecraft.getInstance().getTextureManager().release(textureLocation);
        this.texture.close();
    }
}
