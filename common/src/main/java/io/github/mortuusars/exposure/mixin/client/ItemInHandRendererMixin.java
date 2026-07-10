package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void onRenderHandsWithItems(float partialTicks, PoseStack poseStack, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
        if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
            ci.cancel();
        }
    }
}
