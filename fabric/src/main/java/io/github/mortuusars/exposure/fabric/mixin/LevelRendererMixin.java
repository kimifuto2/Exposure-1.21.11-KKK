package io.github.mortuusars.exposure.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Unique
    private Entity exposure$PlayerEntity;
    @Unique
    private float exposure$PlayerPartialTick;

    @Inject(method = "extractVisibleEntities", at = @At("HEAD"))
    private void beforeExtractVisible(Camera camera, Frustum frustum, DeltaTracker deltaTracker,
            LevelRenderState renderState, CallbackInfo ci) {
        if (!(minecraft.getCameraEntity() instanceof CameraStandEntity)) return;
        Entity player = Minecrft.player();
        if (player == null || player == minecraft.getCameraEntity()) return;
        exposure$PlayerEntity = player;
        exposure$PlayerPartialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
    }

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void onPreSubmitEntities(PoseStack poseStack, LevelRenderState renderState,
            SubmitNodeCollector nodeCollector, CallbackInfo ci) {
        if (exposure$PlayerEntity == null) return;

        var renderer = entityRenderDispatcher.getRenderer(exposure$PlayerEntity);
        if (renderer == null) {
            exposure$PlayerEntity = null;
            return;
        }

        EntityRenderState state = renderer.createRenderState(exposure$PlayerEntity, exposure$PlayerPartialTick);
        if (state != null) {
            var camState = renderState.cameraRenderState;
            entityRenderDispatcher.submit(state, camState,
                camState.pos.x, camState.pos.y, camState.pos.z,
                poseStack, nodeCollector);
        }
        exposure$PlayerEntity = null;
    }
}
