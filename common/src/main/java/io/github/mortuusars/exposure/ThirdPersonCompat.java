package io.github.mortuusars.exposure;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;

/**
 * Compatibility with Leawind's Third Person (mod id: {@code leawind_third_person})
 * and the {@code perspective-api} it is built on.
 *
 * <p>When the Third-Person 360° perspective is active we want the Exposure camera to
 * stay at the current viewfinder position during capture, instead of letting the
 * capture actions force a first-person / selfie camera. Forcing that camera snaps the
 * view to the player's eye for a frame and then the Third-Person camera smoothing
 * smoothly moves it back out, which looks like the viewfinder briefly zooming in and
 * slowly drifting away.
 */
public class ThirdPersonCompat {

    private static final String THIRD_PERSON_MOD_ID = "leawind_third_person";
    private static final String THIRD_PERSON_PERSPECTIVE_ID = "leawind_third_person.third_person";

    private static Boolean savedAutoRotate = null;

    private ThirdPersonCompat() {}

    /** Whether the Leawind Third-Person 360° perspective is currently active. */
    public static boolean isThirdPersonActive() {
        // Prefer the accurate query through perspective-api when it is present.
        try {
            Class<?> api = Class.forName("io.github.leawind.perspectiveapi.api.PerspectiveAPI");
            boolean enabled = (boolean) api.getMethod("isEnabled").invoke(null);
            if (enabled) {
                return (boolean) api.getMethod("isCurrent", String.class)
                        .invoke(null, THIRD_PERSON_PERSPECTIVE_ID);
            }
        } catch (Throwable ignored) {
            // Fall through to the mod-loaded heuristic below.
        }
        // Fallback: if the mod is loaded and the player is not in first person.
        return PlatformHelper.isModLoaded(THIRD_PERSON_MOD_ID)
                && Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;
    }

    /**
     * Called at the head of {@code GameRenderer#render} each frame.
     *
     * <p>Intentionally a no-op: the camera snap during capture is already prevented by
     * guarding the capture actions (they no longer force a first-person camera / reset
     * the camera when this third-person perspective is active).
     */
    public static void onRenderStart() {
    }

    public static void onCameraActivated() {
        if (!PlatformHelper.isModLoaded(THIRD_PERSON_MOD_ID)) return;
        if (savedAutoRotate != null) return;
        try {
            Object settings = getPlayerSettings();
            Method getter = settings.getClass().getMethod("autoRotateInteracting");
            Method setter = settings.getClass().getMethod("setAutoRotateInteracting", boolean.class);
            savedAutoRotate = (Boolean) getter.invoke(settings);
            setter.invoke(settings, false);
        } catch (Throwable ignored) {
            savedAutoRotate = null;
        }
    }

    public static void onCameraDeactivated() {
        if (savedAutoRotate == null) return;
        try {
            Object settings = getPlayerSettings();
            Method setter = settings.getClass().getMethod("setAutoRotateInteracting", boolean.class);
            setter.invoke(settings, savedAutoRotate);
        } catch (Throwable ignored) {}
        savedAutoRotate = null;
    }

    private static Object getPlayerSettings() throws ReflectiveOperationException {
        Class<?> scheduler = Class.forName("io.github.leawind.thirdperson.internal.core.schedule.SchedulerRuntime");
        Object instance = scheduler.getMethod("getInstance").invoke(null);
        return scheduler.getMethod("playerSettings").invoke(instance);
    }
}
