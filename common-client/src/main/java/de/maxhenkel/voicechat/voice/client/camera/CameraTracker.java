package de.maxhenkel.voicechat.voice.client.camera;

import com.mojang.math.Vector3f;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public class CameraTracker {

    private final Minecraft minecraft;
    private volatile CameraState state;

    public CameraTracker() {
        minecraft = Minecraft.getInstance();
        state = CameraState.INITIAL;
        ClientCompatibilityManager.INSTANCE.onRenderTick(this::update);
    }

    private void update() {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        LocalPlayer player = minecraft.player;
        if (!camera.isInitialized() || player == null) {
            state = CameraState.INITIAL;
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        state = new CameraState(
                camera.getPosition(),
                new Vector3f(camera.getLookVector().x(), camera.getLookVector().y(), camera.getLookVector().z()),
                new Vector3f(camera.getUpVector().x(), camera.getUpVector().y(), camera.getUpVector().z()),
                camera.getYRot(),
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
