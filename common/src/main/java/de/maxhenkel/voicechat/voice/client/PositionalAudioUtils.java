package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class PositionalAudioUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * @param soundPos the position of the sound
     * @return a float array of length 2, containing the left and right volume (0-1)
     */
    private static float[] getStereoVolume(Vector3d soundPos) {
        ActiveRenderInfo mainCamera = mc.gameRenderer.getMainCamera();
        Vector3d cameraPos = mainCamera.getPosition();
        Vector3d d = soundPos.subtract(cameraPos).normalize();
        Vector2f diff = new Vector2f((float) d.x, (float) d.z);
        float diffAngle = Utils.angle(diff, new Vector2f(-1F, 0F));
        float angle = Utils.normalizeAngle(diffAngle - (mainCamera.getYRot() % 360F));
        float dif = (float) (Math.abs(cameraPos.y - soundPos.y) / 32); //TODO tweak value

        float rot = angle / 180F;
        float perc = rot;
        if (rot < -0.5F) {
            perc = -(0.5F + (rot + 0.5F));
        } else if (rot > 0.5F) {
            perc = 0.5F - (rot - 0.5F);
        }
        perc = perc * (1 - dif);

        float minVolume = 0.3F;

        float left = perc < 0F ? Math.abs(perc * 1.4F) + minVolume : minVolume;
        float right = perc >= 0F ? (perc * 1.4F) + minVolume : minVolume;

        float fill = 1F - Math.max(left, right);
        left += fill;
        right += fill;

        return new float[]{left, right};
    }

    /**
     * Gets the volume for the provided distance
     *
     * @param clientConnection the voice chat connection
     * @param pos              the position of the audio
     * @return the resulting audio volume
     */
    public static float getDistanceVolume(ClientVoicechatConnection clientConnection, Vector3d pos) {
        return getDistanceVolume(clientConnection, pos, 1F);
    }

    /**
     * Gets the volume for the provided distance
     *
     * @param clientConnection   the voice chat connection
     * @param pos                the position of the audio
     * @param distanceMultiplier a multiplier for the distance
     * @return the resulting audio volume
     */
    public static float getDistanceVolume(ClientVoicechatConnection clientConnection, Vector3d pos, float distanceMultiplier) {
        float distance = (float) pos.distanceTo(mc.cameraEntity.getEyePosition(1F));
        float fadeDistance = (float) clientConnection.getData().getVoiceChatFadeDistance() * distanceMultiplier;
        float maxDistance = (float) clientConnection.getData().getVoiceChatDistance() * distanceMultiplier;

        if (distance < fadeDistance) {
            return 1F;
        } else if (distance > maxDistance) {
            return 0F;
        } else {
            float percentage = (distance - fadeDistance) / (maxDistance - fadeDistance);
            return 1F / (1F + (float) Math.exp((percentage * 12F) - 6F));
        }
    }

    /**
     * Converts 16 bit mono audio to stereo based on the sound position
     * This does not include the volume based on distance
     *
     * @param audio    the audio data
     * @param soundPos the position of the sound - Might be null in case of non-positional audio
     * @return the stereo audio data
     */
    public static short[] convertToStereo(short[] audio, @Nullable Vector3d soundPos) {
        if (soundPos == null) {
            return convertToStereo(audio);
        }
        return convertToStereo(audio, getStereoVolume(soundPos));
    }

    /**
     * Converts 16 bit mono audio to stereo
     *
     * @param audio the audio data
     * @return the adjusted audio
     */
    public static short[] convertToStereo(short[] audio) {
        short[] stereo = new short[audio.length * 2];
        for (int i = 0; i < audio.length; i++) {
            stereo[i * 2] = audio[i];
            stereo[i * 2 + 1] = audio[i];
        }
        return stereo;
    }

    /**
     * Converts 16 bit mono audio to stereo
     *
     * @param audio       the audio data
     * @param volumeLeft  the volume modifier for the left audio
     * @param volumeRight the volume modifier for the right audio
     * @return the adjusted audio
     */
    private static short[] convertToStereo(short[] audio, float volumeLeft, float volumeRight) {
        short[] stereo = new short[audio.length * 2];
        for (int i = 0; i < audio.length; i++) {
            short left = (short) (audio[i] * volumeLeft);
            short right = (short) (audio[i] * volumeRight);
            stereo[i * 2] = left;
            stereo[i * 2 + 1] = right;
        }
        return stereo;
    }

    /**
     * Converts 16 bit mono audio to stereo
     *
     * @param audio   the audio data
     * @param volumes a float array of length 2 containing the left and right volume
     * @return the adjusted audio
     */
    private static short[] convertToStereo(short[] audio, float[] volumes) {
        return convertToStereo(audio, volumes[0], volumes[1]);
    }

    public static short[] convertToStereoForRecording(ClientVoicechatConnection clientConnection, Vector3d pos, short[] monoData) {
        return convertToStereoForRecording(clientConnection, pos, monoData, 1F);
    }

    public static short[] convertToStereoForRecording(ClientVoicechatConnection clientConnection, Vector3d pos, short[] monoData, float distanceMultiplier) {
        float distanceVolume = getDistanceVolume(clientConnection, pos, distanceMultiplier);
        if (!VoicechatClient.CLIENT_CONFIG.audioType.get().equals(AudioType.OFF)) {
            float[] stereoVolume = getStereoVolume(pos);
            return convertToStereo(monoData, distanceVolume * stereoVolume[0], distanceVolume * stereoVolume[1]);
        } else {
            return convertToStereo(monoData, distanceVolume, distanceVolume);
        }
    }

}
