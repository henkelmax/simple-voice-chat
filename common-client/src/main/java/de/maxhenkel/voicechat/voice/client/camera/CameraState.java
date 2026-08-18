package de.maxhenkel.voicechat.voice.client.camera;

import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record CameraState(Vec3 position, Vector3f forward, Vector3f up, float yRot, @Nullable UUID player,
                          Vec3 playerEyePosition, @Nullable UUID cameraEntity, boolean spectator) {

    public static final CameraState INITIAL = new CameraState(Vec3.ZERO, new Vector3f(0F, 0F, -1F), new Vector3f(0F, 1F, 0F), 0F, null, Vec3.ZERO, null, false);

    public boolean detached() {
        return !Objects.equals(player, cameraEntity);
    }

}
