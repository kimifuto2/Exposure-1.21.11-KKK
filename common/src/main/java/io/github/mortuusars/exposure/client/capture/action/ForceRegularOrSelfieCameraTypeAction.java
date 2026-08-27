package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.ThirdPersonCompat;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ForceRegularOrSelfieCameraTypeAction implements CaptureAction {
    private final CameraHolder holder;
    private CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;
    private boolean changed = false;

    public ForceRegularOrSelfieCameraTypeAction(CameraHolder holder) {
        this.holder = holder;
    }

    @Override
    public void beforeCapture() {
        cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();

        // When the Leawind Third-Person 360° perspective is active, don't force a
        // first-person camera (or reset the camera). Doing so snaps the view to the
        // player's eye and then the Third-Person smoothing slowly moves it back out.
        if (ThirdPersonCompat.isThirdPersonActive()) {
            changed = false;
            return;
        }

        if (cameraTypeBeforeCapture == CameraType.THIRD_PERSON_BACK) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            changed = true;
        } else if (cameraTypeBeforeCapture == CameraType.THIRD_PERSON_FRONT && !cameraInHandInSelfieMode()) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            changed = true;
        }
        Minecraft.getInstance().gameRenderer.getMainCamera().reset();
    }

    protected boolean cameraInHandInSelfieMode() {
        @Nullable CameraInHand camera = CameraInHand.find(holder);
        return camera != null && camera.map(CameraItem::isInSelfieMode).orElse(false);
    }

    @Override
    public void afterCapture() {
        if (!changed) return;
        Minecraft.getInstance().options.setCameraType(cameraTypeBeforeCapture);
        changed = false;
    }
}
