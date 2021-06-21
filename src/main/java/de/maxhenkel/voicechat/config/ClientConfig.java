package de.maxhenkel.voicechat.config;

import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import de.maxhenkel.voicechat.voice.client.HUDIconLocation;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;

public class ClientConfig {

    public final ConfigBuilder.ConfigEntry<Double> voiceChatVolume;
    public final ConfigBuilder.ConfigEntry<Double> voiceActivationThreshold;
    public final ConfigBuilder.ConfigEntry<Double> microphoneAmplification;
    public final ConfigBuilder.ConfigEntry<MicrophoneActivationType> microphoneActivationType;
    public final ConfigBuilder.ConfigEntry<Integer> outputBufferSize;
    public final ConfigBuilder.ConfigEntry<Boolean> clearFullAudioBuffer;
    public final ConfigBuilder.ConfigEntry<Integer> deactivationDelay;
    public final ConfigBuilder.ConfigEntry<String> microphone;
    public final ConfigBuilder.ConfigEntry<String> speaker;
    public final ConfigBuilder.ConfigEntry<Boolean> muted;
    public final ConfigBuilder.ConfigEntry<Boolean> disabled;
    public final ConfigBuilder.ConfigEntry<Boolean> stereo;
    public final ConfigBuilder.ConfigEntry<Boolean> hideIcons;
    public final ConfigBuilder.ConfigEntry<Boolean> showGroupHUD;
    public final ConfigBuilder.ConfigEntry<Double> groupHudIconScale;
    public final ConfigBuilder.ConfigEntry<HUDIconLocation> hudIconLocation;
    public final ConfigBuilder.ConfigEntry<GroupPlayerIconOrientation> groupPlayerIconOrientation;

    public ClientConfig(ConfigBuilder builder) {
        voiceChatVolume = builder.doubleEntry("voice_chat_volume", 1D, 0D, 2D);
        voiceActivationThreshold = builder.doubleEntry("voice_activation_threshold", -50D, -127D, 0D);
        microphoneAmplification = builder.doubleEntry("microphone_amplification", 1D, 0D, 4D);
        microphoneActivationType = builder.enumEntry("microphone_activation_type", MicrophoneActivationType.PTT);
        outputBufferSize = builder.integerEntry("output_buffer_size", 6, 1, 16);
        clearFullAudioBuffer = builder.booleanEntry("clear_full_audio_buffer", false);
        deactivationDelay = builder.integerEntry("voice_deactivation_delay", 25, 0, 100);
        microphone = builder.stringEntry("microphone", "");
        speaker = builder.stringEntry("speaker", "");
        muted = builder.booleanEntry("muted", false);
        disabled = builder.booleanEntry("disabled", false);
        stereo = builder.booleanEntry("stereo", true);
        hideIcons = builder.booleanEntry("hide_icons", false);
        showGroupHUD = builder.booleanEntry("show_group_hud", true);
        groupHudIconScale = builder.doubleEntry("group_hud_icon_scale", 2D, 0.01D, 10D);
        hudIconLocation = builder.enumEntry("hud_icon_location", HUDIconLocation.LEFT);
        groupPlayerIconOrientation = builder.enumEntry("group_player_icon_orientation", GroupPlayerIconOrientation.VERTICAL);
    }

}
