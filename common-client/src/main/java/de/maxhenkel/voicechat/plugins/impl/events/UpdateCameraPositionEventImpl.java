package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.internal.events.UpdateCameraPositionEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nullable;

public class UpdateCameraPositionEventImpl extends ClientEventImpl implements UpdateCameraPositionEvent {

    @Nullable
    private Vec3 pos;
    private final Vector3f forward = new Vector3f();
    private final Vector3f up = new Vector3f();

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public void setCameraPosition(Vec3 pos, Vector3fc forward, Vector3fc up) {
        this.pos = pos;
        this.forward.set(forward);
        this.up.set(up);
    }

    public void setPos(@Nullable Vec3 pos) {
        this.pos = pos;
    }

    @Nullable
    public Vec3 getPos() {
        return pos;
    }

    public Vector3f getForward() {
        return forward;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getYRot() {
        return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-forward.x(), forward.z())));
    }

}
