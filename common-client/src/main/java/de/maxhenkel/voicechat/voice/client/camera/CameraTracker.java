package de.maxhenkel.voicechat.voice.client.camera;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class CameraTracker {

    private final Minecraft minecraft;
    private volatile CameraState state;

    public CameraTracker() {
        minecraft = Minecraft.getInstance();
        state = CameraState.INITIAL;
        ClientCompatibilityManager.INSTANCE.onRenderTick(this::update);
    }

    private void update() {
        Camera camera = minecraft.gameRenderer.mainCamera();
        LocalPlayer player = minecraft.player;
        if (!camera.isInitialized() || player == null) {
            state = CameraState.INITIAL;
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        state = new CameraState(
                camera.position(),
                new Vector3f(camera.forwardVector()),
                new Vector3f(camera.upVector()),
                camera.yRot(),
                player.getUUID(),
                player.getEyePosition(),
                cameraEntity == null ? null : cameraEntity.getUUID(),
                player.isSpectator()
        );
    }

    public CameraState getState() {
        return state;
    }

}
