package io.github.mortuusars.exposure.fabric;

import io.github.mortuusars.exposure.Exposure;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PlatformHelperClientImpl {
    private static final Map<Identifier, ExtraModelKey<BlockStateModel>> MODEL_KEYS = new HashMap<>();

    public static void registerModelKey(Identifier model, ExtraModelKey<BlockStateModel> key) {
        MODEL_KEYS.put(model, key);
    }

    public static BlockStateModel getModel(Identifier model) {
        ExtraModelKey<BlockStateModel> key = MODEL_KEYS.get(model);
        if (key != null) {
            BlockStateModel result = ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
            if (result == null) {
                Exposure.LOGGER.warn("[PlatformHelper] Model key found for {} but FabricBakedModelManager returned null", model);
            }
            return result;
        }
        Exposure.LOGGER.warn("[PlatformHelper] No model key registered for {}", model);
        // Fallback
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(
            net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
    }
}
