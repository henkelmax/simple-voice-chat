package de.maxhenkel.voicechat.voice.client.camera;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;

public class CameraTracker {

    private final Minecraft minecraft;
    private volatile CameraState state;

    public CameraTracker() {
        minecraft = Minecraft.getMinecraft();
        state = CameraState.INITIAL;
        ClientCompatibilityManager.INSTANCE.onRenderTick(this::update);
    }

    private void update() {
        CameraState cameraState = ClientPluginManager.instance().onUpdateCameraPosition();
        if (cameraState != null) {
            state = cameraState;
            return;
        }

        EntityPlayerSP player = minecraft.player;
        if (player == null) {
            state = CameraState.INITIAL;
            return;
        }
        Entity cameraEntity = minecraft.getRenderViewEntity();
        state = new CameraState(
                ActiveRenderInfo.getCameraPosition().add(player.getPositionVector()),
                player.rotationYaw,
                player.getUniqueID(),
                player.getPositionEyes(1F),
                cameraEntity == null ? null : cameraEntity.getUniqueID(),
                player.isSpectator()
        );
    }

    public CameraState getState() {
        return state;
    }

}
