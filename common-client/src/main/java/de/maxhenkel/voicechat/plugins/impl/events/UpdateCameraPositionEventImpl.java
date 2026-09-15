package de.maxhenkel.voicechat.plugins.impl.events;

import com.mojang.math.Vector3f;
import de.maxhenkel.voicechat.api.internal.events.UpdateCameraPositionEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

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
    public void setCameraPosition(Vec3 pos, Vector3f forward, Vector3f up) {
        this.pos = pos;
        this.forward.set(forward.x(), forward.y(), forward.z());
        this.up.set(up.x(), up.y(), up.z());
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
