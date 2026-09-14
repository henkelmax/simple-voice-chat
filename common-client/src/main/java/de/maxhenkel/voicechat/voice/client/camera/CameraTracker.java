package de.maxhenkel.voicechat.voice.client.camera;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public class CameraTracker {

    private final Minecraft minecraft;
    private volatile CameraState state;

    public CameraTracker() {
        minecraft = Minecraft.getInstance();
        state = CameraState.INITIAL;
        ClientCompatibilityManager.INSTANCE.onRenderTick(this::update);
    }

    private void update() {
        CameraState cameraState = ClientPluginManager.instance().onUpdateCameraPosition();
        if (cameraState != null) {
            state = cameraState;
            return;
        }

        ActiveRenderInfo camera = minecraft.gameRenderer.getMainCamera();
        ClientPlayerEntity player = minecraft.player;
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
                player.getEyePosition(1F),
                cameraEntity == null ? null : cameraEntity.getUUID(),
                player.isSpectator()
        );
    }

    public CameraState getState() {
        return state;
    }

}
