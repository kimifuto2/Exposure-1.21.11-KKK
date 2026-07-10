package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.item.GlassTint;
import io.github.mortuusars.exposure.client.render.item.conditional.ProjectorActive;
import io.github.mortuusars.exposure.client.render.item.range.AlbumPhotos;
import io.github.mortuusars.exposure.client.render.item.range.Channels;
import io.github.mortuusars.exposure.client.render.item.range.Count;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraAttachments;
import io.github.mortuusars.exposure.client.render.item.select.StackCameraStatus;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.fabric.resources.ExposureFabricClientReloadListener;
import io.github.mortuusars.exposure.network.fabric.FabricS2CPacketHandler;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import java.util.ArrayList;
import java.util.List;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Exposure.ID, ConfigurationScreen::new);

        ItemTintSources.ID_MAPPER.put(Exposure.resource("glass_tint"), GlassTint.MAP_CODEC);

        KeyboardHandler.registerKeymappings(KeyBindingHelper::registerKeyBinding);

        MenuScreens.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);

        // Register extra models using Fabric 1.21.11 API
        ExtraModelKey<BlockStateModel> frameSmallKey = ExtraModelKey.create(ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL::toString);
        ExtraModelKey<BlockStateModel> frameMediumKey = ExtraModelKey.create(ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM::toString);
        ExtraModelKey<BlockStateModel> frameLargeKey = ExtraModelKey.create(ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE::toString);
        ExtraModelKey<BlockStateModel> clearFrameSmallKey = ExtraModelKey.create(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_SMALL::toString);
        ExtraModelKey<BlockStateModel> clearFrameMediumKey = ExtraModelKey.create(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_MEDIUM::toString);
        ExtraModelKey<BlockStateModel> clearFrameLargeKey = ExtraModelKey.create(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_LARGE::toString);
        ExtraModelKey<BlockStateModel> cameraStandKey = ExtraModelKey.create(ExposureClient.Models.CAMERA_STAND::toString);
        ExtraModelKey<BlockStateModel> cameraStandMountKey = ExtraModelKey.create(ExposureClient.Models.CAMERA_STAND_MOUNT::toString);
        ExtraModelKey<BlockStateModel> cameraItemKey = ExtraModelKey.create(ExposureClient.Models.CAMERA_ITEM::toString);

        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL, frameSmallKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM, frameMediumKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE, frameLargeKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_SMALL, clearFrameSmallKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_MEDIUM, clearFrameMediumKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_LARGE, clearFrameLargeKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CAMERA_STAND, cameraStandKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CAMERA_STAND_MOUNT, cameraStandMountKey);
        PlatformHelperClientImpl.registerModelKey(ExposureClient.Models.CAMERA_ITEM, cameraItemKey);

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModel(frameSmallKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL));
            pluginContext.addModel(frameMediumKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM));
            pluginContext.addModel(frameLargeKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE));
            pluginContext.addModel(clearFrameSmallKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_SMALL));
            pluginContext.addModel(clearFrameMediumKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_MEDIUM));
            pluginContext.addModel(clearFrameLargeKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CLEAR_PHOTOGRAPH_FRAME_LARGE));
            pluginContext.addModel(cameraStandKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CAMERA_STAND));
            pluginContext.addModel(cameraStandMountKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CAMERA_STAND_MOUNT));
            pluginContext.addModel(cameraItemKey, SimpleUnbakedExtraModel.blockStateModel(ExposureClient.Models.CAMERA_ITEM));
        });

        SelectItemModelProperties.ID_MAPPER.put(ExposureClient.SelectProperties.CAMERA_STATUS, StackCameraStatus.TYPE);
        SelectItemModelProperties.ID_MAPPER.put(ExposureClient.SelectProperties.CAMERA_ATTACHMENTS, StackCameraAttachments.TYPE);

        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.ALBUM_PHOTOS, AlbumPhotos.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.CHANNELS, Channels.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(ExposureClient.RangeSelectProperties.COUNT, Count.MAP_CODEC);

        ConditionalItemModelProperties.ID_MAPPER.put(ExposureClient.ConditionalProperties.PROJECTOR_ACTIVE, ProjectorActive.MAP_CODEC);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ExposureFabricClientReloadListener());

        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), GlassPhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CAMERA_STAND.get(), CameraStandEntityRenderer::new);

        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip
                ? new PhotographClientTooltip(photographTooltip) : null);

        HudRenderCallback.EVENT.register(CameraStandTooltip::render);

        FabricS2CPacketHandler.register();
    }
}
