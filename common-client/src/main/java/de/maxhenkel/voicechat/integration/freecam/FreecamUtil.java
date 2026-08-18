package de.maxhenkel.voicechat.integration.freecam;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import de.maxhenkel.voicechat.voice.client.camera.CameraState;
import net.minecraft.world.phys.Vec3;

public class FreecamUtil {

    /**
     * @param camera the camera to check
     * @return whether freecam is currently in use
     */
    public static boolean isFreecamEnabled(CameraState camera) {
        return VoicechatClient.CLIENT_CONFIG.freecamMode.get().equals(FreecamMode.PLAYER) && !camera.spectator() && camera.detached();
    }

    /**
     * Gets the proximity reference point. Unless freecam is active, this is the main camera's position.
     *
     * @param camera the camera to measure from
     * @return the position distances should be measured from
     */
    public static Vec3 getReferencePoint(CameraState camera) {
        return isFreecamEnabled(camera) ? camera.playerEyePosition() : camera.position();
    }

    /**
     * Measures the distance to the provided position.
     * <p>
     * Distance is relative to either the player or camera, depending on whether freecam is enabled.
     *
     * @param camera the camera to measure from
     * @param pos    the position to be measured
     * @return the distance to the position
     */
    public static double getDistanceTo(CameraState camera, Vec3 pos) {
        return getReferencePoint(camera).distanceTo(pos);
    }

    /**
     * Gets the volume for the provided distance.
     * <p>
     * Distance is relative to either the player or camera, depending on whether freecam is enabled.
     *
     * @param camera      the camera to measure from
     * @param maxDistance the maximum distance of the sound
     * @param pos         the position of the audio
     * @return the resulting audio volume
     */
    public static float getDistanceVolume(CameraState camera, float maxDistance, Vec3 pos) {
        return PositionalAudioUtils.getDistanceVolume(maxDistance, getReferencePoint(camera), pos);
    }
}
