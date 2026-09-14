package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.UpdateCameraPositionEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

public class UpdateCameraPositionEventImpl extends ClientEventImpl implements UpdateCameraPositionEvent {

    @Nullable
    private Vector3d pos;
    private final Vector3f forward = new Vector3f();
    private final Vector3f up = new Vector3f();

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public void setCameraPosition(Vector3d pos, Vector3f forward, Vector3f up) {
        this.pos = pos;
        this.forward.set(forward.x(), forward.y(), forward.z());
        this.up.set(up.x(), up.y(), up.z());
    }

    public void setPos(@Nullable Vector3d pos) {
        this.pos = pos;
    }

    @Nullable
    public Vector3d getPos() {
        return pos;
    }

    public Vector3f getForward() {
        return forward;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getYRot() {
        return wrapDegrees((float) Math.toDegrees(Math.atan2(-forward.x(), forward.z())));
    }

    public static float wrapDegrees(final float angle) {
        float normalizedAngle = angle % 360F;
        if (normalizedAngle >= 180F) {
            normalizedAngle -= 360F;
        }
        if (normalizedAngle < -180F) {
            normalizedAngle += 360F;
        }
        return normalizedAngle;
    }

}
