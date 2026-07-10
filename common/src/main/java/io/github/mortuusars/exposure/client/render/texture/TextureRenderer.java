package io.github.mortuusars.exposure.client.render.texture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class TextureRenderer {
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, Color color) {
        render(poseStack, bufferSource, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        // Use RenderType.text() → gbuffers_textured in Iris (simple lighting, no PBR/SSR)
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderTypes.text(texture));
        bufferBuilder.addVertex(matrix, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
        bufferBuilder.addVertex(matrix, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, Identifier texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, nodeCollector, texture, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector nodeCollector, Identifier texture,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        // Use RenderTypes.text() → gbuffers_textured in Iris (simple lighting, no PBR/SSR)
        // Vertex format: Position + Color + UV0 + UV2(lightmap) only
        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(texture), (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(pose, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(pose, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
            vertexConsumer.addVertex(pose, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
        });
    }
}
