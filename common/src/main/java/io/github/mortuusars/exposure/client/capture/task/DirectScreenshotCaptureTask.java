package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.client.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DirectScreenshotCaptureTask extends Task<Result<Image>> {
    protected int delay = Math.max(1, Config.Client.DIRECT_CAPTURE_DELAY_FRAMES.get());

    @Nullable
    protected CompletableFuture<Result<Image>> future;
    protected boolean capturing;

    @Override
    public @NotNull CompletableFuture<Result<Image>> execute() {
        if (future == null) {
            future = new CompletableFuture<>();
        }
        return future;
    }

    @Override
    public void tick() {
        if (future == null || future.isDone()) {
            return;
        }

        if (delay <= 0 && !capturing) {
            capturing = true;
            Exposure.LOGGER.info("[Exposure] Taking screenshot");
            try {
                Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(), img -> {
                    if (img != null) {
                        int cp = img.getPixel(img.getWidth() / 2, img.getHeight() / 2);
                        int topLeft = img.getPixel(0, 0);
                        Exposure.LOGGER.info("[Exposure] Screenshot: {}x{}, center=0x{}, topLeft=0x{}",
                            img.getWidth(), img.getHeight(), Integer.toHexString(cp), Integer.toHexString(topLeft));
                        future.complete(Result.success(new WrappedNativeImage(img)));
                    } else {
                        Exposure.LOGGER.error("[Exposure] Screenshot returned null - cannot capture");
                        future.complete(Result.error(new io.github.mortuusars.exposure.util.TranslatableError("exposure.screenshot.null", "Screenshot returned null")));
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }

        delay--;
    }
}
