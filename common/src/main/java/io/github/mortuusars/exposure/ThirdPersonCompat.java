package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.util.Minecrft;

/**
 * Disables ThirdPerson's auto_rotate_interacting when Exposure camera is active,
 * preventing the right-click-to-turn behavior during photo capture.
 */
public class ThirdPersonCompat {
    private static Boolean savedAutoRotate = null;

    public static void onCameraActivated() {
        if (!PlatformHelper.isModLoaded("leawind_third_person")) return;
        try {
            Class<?> tpClass = Class.forName("com.github.leawind.thirdperson.ThirdPerson");
            Object config = tpClass.getMethod("getConfig").invoke(null);
            var field = config.getClass().getField("auto_rotate_interacting");
            savedAutoRotate = (Boolean) field.get(config);
            field.set(config, false);
        } catch (Exception ignored) {}
    }

    public static void onCameraDeactivated() {
        if (savedAutoRotate == null) return;
        try {
            Class<?> tpClass = Class.forName("com.github.leawind.thirdperson.ThirdPerson");
            Object config = tpClass.getMethod("getConfig").invoke(null);
            var field = config.getClass().getField("auto_rotate_interacting");
            field.set(config, savedAutoRotate);
        } catch (Exception ignored) {}
        savedAutoRotate = null;
    }
}
