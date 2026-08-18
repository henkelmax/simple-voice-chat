package de.maxhenkel.voicechat.voice.client.camera;

import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class CameraState {

    public static final CameraState INITIAL = new CameraState(Vec3d.ZERO, 0F, null, Vec3d.ZERO, null, false);

    private final Vec3d position;
    private final float yRot;
    @Nullable
    private final UUID player;
    private final Vec3d playerEyePosition;
    @Nullable
    private final UUID cameraEntity;
    private final boolean spectator;

    public CameraState(Vec3d position, float yRot, @Nullable UUID player, Vec3d playerEyePosition, @Nullable UUID cameraEntity, boolean spectator) {
        this.position = position;
        this.yRot = yRot;
        this.player = player;
        this.playerEyePosition = playerEyePosition;
        this.cameraEntity = cameraEntity;
        this.spectator = spectator;
    }

    public Vec3d position() {
        return position;
    }

    public float yRot() {
        return yRot;
    }

    @Nullable
    public UUID player() {
        return player;
    }

    public Vec3d playerEyePosition() {
        return playerEyePosition;
    }

    @Nullable
    public UUID cameraEntity() {
        return cameraEntity;
    }

    public boolean spectator() {
        return spectator;
    }

    public boolean detached() {
        return !Objects.equals(player, cameraEntity);
    }

}
