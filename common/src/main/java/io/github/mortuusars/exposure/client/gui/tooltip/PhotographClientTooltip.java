package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderedImageInstance;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographClientTooltip implements ClientTooltipComponent {
    public static final int SIZE = 72;

    protected final PhotographTooltip tooltip;
    protected final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographClientTooltip(PhotographTooltip tooltip) {
        this.tooltip = tooltip;
        this.photographs = tooltip.photographs();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return SIZE + 2;
    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, int width, int height, GuiGraphics guiGraphics) {
        int photographsCount = photographs.size();
        int additionalPhotographs = Math.min(2, photographsCount - 1);

        float nextPhotographOffset = ExposureClient.photographRenderer().getStackedPhotographOffset();
        int renderSize = (int) (SIZE * (1f - (additionalPhotographs * nextPhotographOffset)));

        renderPhotographStack(guiGraphics, mouseX, mouseY, renderSize, photographs);

        if (photographsCount > 1) {
            guiGraphics.pose().pushMatrix();
            String count = Integer.toString(photographsCount);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            guiGraphics.pose().translate(
                    mouseX + renderSize - 2 - fontWidth * fontScale,
                    mouseY + renderSize - 2 - 8 * fontScale);
            guiGraphics.pose().scale(fontScale, fontScale);
            guiGraphics.drawString(font, count, 0, 0, 0xFFFFFFFF);
            guiGraphics.pose().popMatrix();
        }
    }

    private void renderPhotographStack(GuiGraphics guiGraphics, int x, int y, int size,
                                       List<ItemAndStack<PhotographItem>> photos) {
        if (photos.isEmpty()) return;

        for (int i = Math.min(2, photos.size() - 1); i >= 0; i--) {
            ItemAndStack<PhotographItem> photograph = photos.get(i);
            int offset = (int) (ExposureClient.photographRenderer().getStackedPhotographOffset() * size * i);

            if (i == 0) {
                renderSinglePhotograph(guiGraphics, x + offset, y + offset, size, photograph);
                break;
            }

            PhotographStyle style = PhotographStyle.of(photograph.getItemStack());
            if (style.paperTexture() != ExposureClient.Textures.EMPTY) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, style.paperTexture(),
                        x + offset, y + offset, 0, 0, size, size, size, size);
            }
        }
    }

    private void renderSinglePhotograph(GuiGraphics guiGraphics, int x, int y, int size,
                                        ItemAndStack<PhotographItem> photograph) {
        PhotographStyle style = PhotographStyle.of(photograph.getItemStack());
        RenderableImage image = style.process(
                ExposureClient.renderedExposures().getOrCreate(photograph.getItem().getFrame(photograph.getItemStack())));

        if (style.paperTexture() != ExposureClient.Textures.EMPTY) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, style.paperTexture(),
                    x, y, 0, 0, size, size, size, size);
        }

        RenderedImageInstance instance = ExposureClient.imageRenderer().getOrCreateInstance(image);
        instance.ensureUploaded();
        int margin = size / 16;
        int imageSize = size - margin * 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, image.getIdentifier().toIdentifier(),
                x + margin, y + margin, 0, 0, imageSize, imageSize, imageSize, imageSize);

        if (style.hasOverlayTexture()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, style.overlayTexture(),
                    x, y, 0, 0, size, size, size, size);
        }
    }
}
