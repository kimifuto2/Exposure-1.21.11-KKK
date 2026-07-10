package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.render.state.CameraStandEntityRenderState;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public class CameraStandEntityRenderer <T extends CameraStandEntity> extends EntityRenderer<T, CameraStandEntityRenderState> {
    public static final float MOUNT_SCALE = 0.9f;

    protected final BlockRenderDispatcher blockRenderer;
    protected final ItemModelResolver itemResolver;

    public CameraStandEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.itemResolver = context.getItemModelResolver();
    }

    @Override
    public @NotNull CameraStandEntityRenderState createRenderState() {
        return new CameraStandEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, CameraStandEntityRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.hurtDir = entity.getHurtDir();
        reusedState.hurtTime = (float)entity.getHurtTime() - partialTick;
        reusedState.damageTime = Math.max(0, entity.getDamage() - partialTick);
        reusedState.entityPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        reusedState.entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        reusedState.inVehicle = entity.getVehicle() != null;
        reusedState.camera = entity.getCamera();
        reusedState.isPlayerControlled = entity.isCameraActive();
        reusedState.entityReference = entity;
        if (reusedState.inVehicle) {
            reusedState.vehicleRot = Mth.lerp(partialTick, entity.getVehicle().yRotO, entity.getVehicle().getYRot());
        }

    }

    @Override
    public void submit(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {

        float hurtTime = state.hurtTime;
        float damage = state.damageTime;
        if (hurtTime > 0.0F) {
            float rotation = Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) state.hurtDir;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        }

        float entityPitch = state.entityPitch;

        renderStand(state, poseStack, nodeCollector, state.lightCoords);
        renderMount(state, poseStack, nodeCollector, state.lightCoords);
        if (!state.camera.isEmpty()) {
            renderCamera(state, poseStack, nodeCollector, state.lightCoords);
        }
    }

    private void renderStand(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();

        if (state.inVehicle) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.vehicleRot + 45));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);

        Identifier modelLocation = ExposureClient.Models.CAMERA_STAND;
        BlockStateModel model = PlatformHelperClient.getModel(modelLocation);
        bufferSource.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (pose, vertexConsumer) -> {
            blockRenderer.getModelRenderer().renderModel(pose, vertexConsumer, model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        });
        poseStack.popPose();
    }

    private void renderMount(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.125, 0);
        float scale = MOUNT_SCALE;
        poseStack.scale(scale, scale, scale);

        float entityYaw = state.entityYaw;
        float entityPitch = state.entityPitch;

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-10));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);
        Identifier mountModelLocation = ExposureClient.Models.CAMERA_STAND_MOUNT;
        BlockStateModel mountModel = PlatformHelperClient.getModel(mountModelLocation);
        bufferSource.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (pose, vertexConsumer) -> {
            blockRenderer.getModelRenderer().renderModel(pose, vertexConsumer, mountModel, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        });
        poseStack.popPose();
    }

    private void renderCamera(CameraStandEntityRenderState state, PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight) {
        poseStack.pushPose();

        float entityYaw = state.entityYaw;
        float entityPitch = state.isPlayerControlled ? state.entityPitch : 0f;

        poseStack.translate(0, 1.125, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw + 180));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entityPitch));
        poseStack.translate(0, 0.125 * MOUNT_SCALE, 0);

        if (state.isMalfunctioned) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
            poseStack.mulPose(Axis.XP.rotationDegrees(-15));
        }

        ItemStack camera = state.camera;
        float scale = camera.getItem() instanceof CameraItem cameraItem ? cameraItem.getScaleOnStand() : MOUNT_SCALE;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.49, 0, -0.45);

        ItemStackRenderState renderState = new ItemStackRenderState();
        itemResolver.updateForTopItem(renderState, camera, ItemDisplayContext.NONE, Minecrft.level(), null, 0);
        itemResolver.appendItemLayers(renderState, camera, ItemDisplayContext.NONE, Minecrft.level(), null, 0);
        
        MultiBufferSource.BufferSource buf = Minecrft.get().renderBuffers().bufferSource();
        try {
            // Debug: dump all field names
            Exposure.LOGGER.info("[CameraStand] ItemStackRenderState fields:");
            for (java.lang.reflect.Field f : renderState.getClass().getDeclaredFields()) {
                Exposure.LOGGER.info("[CameraStand]   {} ({})", f.getName(), f.getType().getSimpleName());
            }
            
            Field layersField = renderState.getClass().getDeclaredField("field_55340");
            layersField.setAccessible(true);
            Object[] layers = (Object[]) layersField.get(renderState);
            
            Exposure.LOGGER.info("[CameraStand] Found {} layers", layers.length);
            
            // Dump inner class fields (first layer)
            if (layers.length > 0 && layers[0] != null) {
                Object firstLayer = layers[0];
                Exposure.LOGGER.info("[CameraStand] LayerRenderState fields:");
                for (java.lang.reflect.Field f : firstLayer.getClass().getDeclaredFields()) {
                    Exposure.LOGGER.info("[CameraStand]   {} ({})", f.getName(), f.getType().getSimpleName());
                }
            }
            
            for (Object layer : layers) {
                if (layer == null) continue;
                // field_55348 = quads list (ObjectArrayList<BakedQuad>)
                // field_55349 = int[] tints
                // field_55350 = RenderType
                // Try field_56964 (List) first, then fallback to field_55348 via FastUtil elements()
                List<BakedQuad> quads;
                try {
                    Field listField = layer.getClass().getDeclaredField("field_56964");
                    listField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<BakedQuad> quadList = (List<BakedQuad>) listField.get(layer);
                    quads = quadList;
                } catch (Exception e) {
                    Field quadsField = layer.getClass().getDeclaredField("field_55348");
                    quadsField.setAccessible(true);
                    Object quadsObj = quadsField.get(layer);
                    java.lang.reflect.Method elements = quadsObj.getClass().getMethod("elements");
                    BakedQuad[] quadArray = (BakedQuad[]) elements.invoke(quadsObj);
                    quads = java.util.Arrays.asList(quadArray);
                }
                
                // Try to get renderType from various fields
                net.minecraft.client.renderer.rendertype.RenderType renderType = null;
                try {
                    Field typeField = layer.getClass().getDeclaredField("field_55347");
                    typeField.setAccessible(true);
                    renderType = (net.minecraft.client.renderer.rendertype.RenderType) typeField.get(layer);
                } catch (Exception e1) {
                    try {
                        Field typeField = layer.getClass().getDeclaredField("field_55350");
                        typeField.setAccessible(true);
                        renderType = (net.minecraft.client.renderer.rendertype.RenderType) typeField.get(layer);
                    } catch (Exception e2) {
                        // fallback: use renderTypeGetter
                        Field getterField = layer.getClass().getDeclaredField("renderTypeGetter");
                        getterField.setAccessible(true);
                        Object getter = getterField.get(layer);
                        java.lang.reflect.Method getMethod = getter.getClass().getMethod("get");
                        renderType = (net.minecraft.client.renderer.rendertype.RenderType) getMethod.invoke(getter);
                    }
                }
                
                Field tintsField = layer.getClass().getDeclaredField("field_55349");
                tintsField.setAccessible(true);
                int[] tints = (int[]) tintsField.get(layer);
                
                Exposure.LOGGER.info("[CameraStand] Layer with {} quads, renderType={}", quads.size(), renderType);
                
                if (!quads.isEmpty() && renderType != null) {
                    ItemRenderer.renderItem(ItemDisplayContext.NONE, poseStack, buf, packedLight, OverlayTexture.NO_OVERLAY, tints, quads, renderType, ItemStackRenderState.FoilType.NONE);
                }
            }
        } catch (Exception e) {
            Exposure.LOGGER.error("Failed to render camera on stand: {}", e.toString(), e);
        }
        buf.endBatch();

        poseStack.popPose();
    }

    private static Field findField(Class<?> clazz, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException("None of " + java.util.Arrays.toString(names) + " found in " + clazz.getName());
    }
}
