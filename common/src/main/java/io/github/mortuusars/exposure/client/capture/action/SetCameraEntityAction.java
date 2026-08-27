package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.ThirdPersonCompat;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.world.entity.Entity;

public class SetCameraEntityAction implements CaptureAction {
    private final Entity cameraEntity;
    private Entity cameraEntityBeforeCapture;
    private boolean changed = false;

    public SetCameraEntityAction(Entity cameraEntity) {
        this.cameraEntity = cameraEntity;
        this.cameraEntityBeforeCapture = Minecrft.player();
    }

    @Override
    public void beforeCapture() {
        cameraEntityBeforeCapture = Minecrft.get().getCameraEntity();

        // When the Leawind Third-Person 360° perspective is active and this is a hand-held
        // camera (target entity is the player), don't re-set the camera entity. Doing so
        // resets the camera and snaps the 360° view, then it smoothly drifts back out.
        if (ThirdPersonCompat.isThirdPersonActive() && cameraEntity == Minecrft.player()) {
            changed = false;
            return;
        }

        CameraClient.setCameraEntity(cameraEntity);
        changed = true;
    }

    @Override
    public void afterCapture() {
        if (!changed) return;
        CameraClient.setCameraEntity(cameraEntityBeforeCapture);
        changed = false;
    }
}
