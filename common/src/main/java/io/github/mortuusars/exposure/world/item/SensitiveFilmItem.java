package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.film.properties.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface SensitiveFilmItem extends FilmItem {
    default @NotNull FilmProperties getFilmProperties(ItemStack stack) {
        return new FilmProperties(getType(), Optional.of(getFrameSize(stack)), getColorPalette(stack), getDitherMode(stack), getFilmStyle(stack));
    }

    default @NotNull FilmStyle getFilmStyle(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FILM_STYLE, FilmStyle.EMPTY);
    }

    default ResourceKey<ColorPalette> getColorPalette(ItemStack stack) {
        @Nullable Identifier location = stack.get(Exposure.DataComponents.FILM_COLOR_PALETTE);
        if (location != null) return ColorPalettes.createKey(location);
        return ColorPalettes.DEFAULT;
    }

    default @NotNull DitherMode getDitherMode(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.FILM_DITHER_MODE, DitherMode.DITHERED);
    }

    // --

    default void addFrameSizeToTooltip(ItemStack stack, Consumer<Component> tooltipComponents) {
        int frameSize = getFrameSize(stack);
        if (frameSize != getDefaultFrameSize(stack)) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.frame_size",
                            Component.literal(String.format("%.1f", frameSize / 10f)))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    default void addPaletteToTooltip(ItemStack stack, Consumer<Component> tooltipComponents) {
        ResourceKey<ColorPalette> palette = getColorPalette(stack);
        if (!palette.equals(ColorPalettes.DEFAULT)) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.palette", palette.identifier().toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    default void addStyleToTooltip(ItemStack stack, Consumer<Component> tooltipComponents) {
        FilmStyle style = getFilmStyle(stack);
        if (style.sensitivity() != 0) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.sensitivity", style.sensitivity())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (style.contrast() != 0) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.contrast", style.contrast())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (style.levels() != Levels.EMPTY) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.levels", style.levels().toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (style.hsb() != HSB.EMPTY) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.hsb", style.hsb().toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (style.colorBalance() != ColorBalance.EMPTY) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.color_balance", style.colorBalance().toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (style.noise() != 0f) {
            tooltipComponents.accept(Component.translatable("item.exposure.film_roll.tooltip.noise", style.noise())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
