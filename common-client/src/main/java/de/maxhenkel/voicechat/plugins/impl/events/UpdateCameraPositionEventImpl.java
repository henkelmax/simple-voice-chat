package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.UpdateCameraPositionEvent;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class UpdateCameraPositionEventImpl extends ClientEventImpl implements UpdateCameraPositionEvent {

    @Nullable
    private Vec3d pos;
    private float yRot;

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public void setCameraPosition(Vec3d pos, Vec3d forward, Vec3d up) {
        this.pos = pos;
        this.yRot = wrapDegrees((float) Math.toDegrees(Math.atan2(-forward.x, forward.z)));
    }

    public void setPos(@Nullable Vec3d pos) {
        this.pos = pos;
    }

    @Nullable
    public Vec3d getPos() {
        return pos;
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
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
