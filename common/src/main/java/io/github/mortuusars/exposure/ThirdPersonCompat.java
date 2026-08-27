package io.github.mortuusars.exposure;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

/**
 * Compatibility with Leawind's Third Person (mod id: {@code leawind_third_person}).
 *
 * <p>Targets the deployed 2.5.0 build (package {@code com.github.leawind.thirdperson}),
 * which exposes the config through {@code ThirdPerson.getConfig()} and stores the
 * auto-rotate-interacting flag as a public {@code AbstractConfig#auto_rotate_interacting}
 * field. We pause it while the Exposure camera is active so right-clicking to take a
 * photo does not instantly turn the player towards the crosshair.
 */
public class ThirdPersonCompat {

    private static final String THIRD_PERSON_MOD_ID = "leawind_third_person";

    private static Boolean savedAutoRotate = null;

    private ThirdPersonCompat() {}

    /** Whether the Leawind Third-Person mod is loaded and currently in a third-person camera. */
    public static boolean isThirdPersonActive() {
        if (!PlatformHelper.isModLoaded(THIRD_PERSON_MOD_ID)) return false;
        return Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;
    }

    /**
     * Called at the head of {@code GameRenderer#render} each frame.
     *
     * <p>Intentionally a no-op: the camera snap during capture is already prevented by
     * guarding the capture actions (see {@code ForceRegularOrSelfieCameraTypeAction} and
     * {@code SetCameraEntityAction}).
     */
    public static void onRenderStart() {
    }

    public static void onCameraActivated() {
        if (!PlatformHelper.isModLoaded(THIRD_PERSON_MOD_ID)) return;
        if (savedAutoRotate != null) return;
        try {
            Class<?> tpClass = Class.forName("com.github.leawind.thirdperson.ThirdPerson");
            Object config = tpClass.getMethod("getConfig").invoke(null);
            Field field = config.getClass().getField("auto_rotate_interacting");
            savedAutoRotate = (Boolean) field.get(config);
            field.set(config, false);
        } catch (Throwable ignored) {
            savedAutoRotate = null;
        }
    }

    public static void onCameraDeactivated() {
        if (savedAutoRotate == null) return;
        try {
            Class<?> tpClass = Class.forName("com.github.leawind.thirdperson.ThirdPerson");
            Object config = tpClass.getMethod("getConfig").invoke(null);
            Field field = config.getClass().getField("auto_rotate_interacting");
            field.set(config, savedAutoRotate);
        } catch (Throwable ignored) {}
        savedAutoRotate = null;
    }
}
