package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;

import java.util.function.Supplier;

public record KeyBinding(Key matcher, Supplier<Boolean> handler) {
    public boolean keyPressed(KeyEvent event) {
        return matcher.matches(event) && handler().get();
    }

    public boolean keyReleased(KeyEvent event) {
        return matcher.matches(event) && handler().get();
    }
}
