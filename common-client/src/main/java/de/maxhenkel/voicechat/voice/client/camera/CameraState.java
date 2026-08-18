package de.maxhenkel.voicechat.voice.client.camera;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class CameraState {

    public static final CameraState INITIAL = new CameraState(Vector3d.ZERO, new Vector3f(0F, 0F, -1F), new Vector3f(0F, 1F, 0F), 0F, null, Vector3d.ZERO, null, false);

    private final Vector3d position;
    private final Vector3f forward;
    private final Vector3f up;
    private final float yRot;
    @Nullable
    private final UUID player;
    private final Vector3d playerEyePosition;
    @Nullable
    private final UUID cameraEntity;
    private final boolean spectator;

    public CameraState(Vector3d position, Vector3f forward, Vector3f up, float yRot, @Nullable UUID player, Vector3d playerEyePosition, @Nullable UUID cameraEntity, boolean spectator) {
        this.position = position;
        this.forward = forward;
        this.up = up;
        this.yRot = yRot;
        this.player = player;
        this.playerEyePosition = playerEyePosition;
        this.cameraEntity = cameraEntity;
        this.spectator = spectator;
    }

    public Vector3d position() {
        return position;
    }

    public Vector3f forward() {
        return forward;
    }

    public Vector3f up() {
        return up;
    }

    public float yRot() {
        return yRot;
    }

    @Nullable
    public UUID player() {
        return player;
    }

    public Vector3d playerEyePosition() {
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
