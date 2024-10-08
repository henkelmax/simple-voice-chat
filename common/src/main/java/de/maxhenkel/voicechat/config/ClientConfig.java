package de.maxhenkel.voicechat.config;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.integration.freecam.FreecamMode;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;

public class ClientConfig {

    public ConfigEntry<Boolean> onboardingFinished;
    public ConfigEntry<Double> voiceChatVolume;
    public ConfigEntry<Double> voiceActivationThreshold;
    public ConfigEntry<Double> microphoneAmplification;
    public ConfigEntry<MicrophoneActivationType> microphoneActivationType;
    public ConfigEntry<Integer> outputBufferSize;
    public ConfigEntry<Integer> audioPacketThreshold;
    public ConfigEntry<Integer> deactivationDelay;
    public ConfigEntry<String> microphone;
    public ConfigEntry<String> speaker;
    public ConfigEntry<Boolean> muted;
    public ConfigEntry<Boolean> disabled;
    public ConfigEntry<Boolean> hideIcons;
    public ConfigEntry<Boolean> showGroupHUD;
    public ConfigEntry<Boolean> showOwnGroupIcon;
    public ConfigEntry<Double> groupHudIconScale;
    public ConfigEntry<GroupPlayerIconOrientation> groupPlayerIconOrientation;
    public ConfigEntry<Integer> groupPlayerIconPosX;
    public ConfigEntry<Integer> groupPlayerIconPosY;
    public ConfigEntry<Integer> hudIconPosX;
    public ConfigEntry<Integer> hudIconPosY;
    public ConfigEntry<Double> hudIconScale;
    public ConfigEntry<String> recordingDestination;
    public ConfigEntry<Integer> recordingQuality;
    public ConfigEntry<Boolean> denoiser;
    public ConfigEntry<Boolean> runLocalServer;
    public ConfigEntry<Boolean> javaMicrophoneImplementation;
    public ConfigEntry<Boolean> showFakePlayersDisconnected;
    public ConfigEntry<Boolean> offlinePlayerVolumeAdjustment;
    public ConfigEntry<AudioType> audioType;
    public ConfigEntry<Boolean> useNatives;
    public ConfigEntry<FreecamMode> freecamMode;
    public ConfigEntry<Boolean> muteOnJoin;

    public ClientConfig(ConfigBuilder builder) {

        builder.header(String.format("%s client config v%s", CommonCompatibilityManager.INSTANCE.getModName(), CommonCompatibilityManager.INSTANCE.getModVersion()));

        onboardingFinished = builder
                .booleanEntry("onboarding_finished", false,
                        "If the voice chat onboarding process has been finished"
                );
        voiceChatVolume = builder
                .doubleEntry("voice_chat_volume", 1D, 0D, 2D,
                        "The voice chat volume"
                );
        voiceActivationThreshold = builder
                .doubleEntry("voice_activation_threshold", -50D, -127D, 0D,
                        "The threshold for the voice activation method (in dB)"
                );
        microphoneAmplification = builder
                .doubleEntry("microphone_amplification", 1D, 0D, 4D,
                        "The voice chat microphone amplification"
                );
        microphoneActivationType = builder
                .enumEntry("microphone_activation_type", MicrophoneActivationType.PTT,
                        "The microphone activation method",
                        "Valid values are 'PTT' and 'VOICE'"
                );
        outputBufferSize = builder
                .integerEntry("output_buffer_size", 5, 1, 16,
                        "The size of the audio output buffer (in packets)",
                        "Higher values mean a higher latency but less crackling",
                        "Increase this value if you have an unstable internet connection"
                );
        audioPacketThreshold = builder
                .integerEntry("audio_packet_threshold", 3, 0, 16,
                        "The maximum number of audio packets that should be held back if a packet arrives out of order or is dropped",
                        "This prevents audio packets that are only slightly out of order from being discarded",
                        "Set this to 0 to disable"
                );
        deactivationDelay = builder
                .integerEntry("voice_deactivation_delay", 25, 0, 100,
                        "The time it takes for the microphone to deactivate when using voice activation",
                        "A value of 1 means 20 milliseconds, 2=40 ms, 3=60 ms, and so on"
                );
        microphone = builder
                .stringEntry("microphone", "",
                        "The microphone used by the voice chat",
                        "Leave blank to use the default device"
                );
        speaker = builder
                .stringEntry("speaker", "",
                        "The speaker used by the voice chat",
                        "Leave blank to use the default device"
                );
        muted = builder
                .booleanEntry("muted", true,
                        "If the microphone is muted (only relevant for the voice activation method)"
                );
        disabled = builder
                .booleanEntry("disabled", false,
                        "If the voice chat is disabled (both sound and microphone off)"
                );
        hideIcons = builder
                .booleanEntry("hide_icons", false,
                        "If the voice chat HUD, group chat HUD, and other in-game icons should be hidden"
                );
        showGroupHUD = builder
                .booleanEntry("show_group_hud", true,
                        "If the group chat HUD should be visible"
                );
        showOwnGroupIcon = builder
                .booleanEntry("show_own_group_icon", true,
                        "If your own player icon should be displayed in the group chat HUD when you are in a group"
                );
        groupHudIconScale = builder
                .doubleEntry("group_hud_icon_scale", 2D, 0.01D, 10D,
                        "The scale of the player icons in the group chat HUD"
                );
        groupPlayerIconOrientation = builder
                .enumEntry("group_player_icon_orientation", GroupPlayerIconOrientation.VERTICAL,
                        "The orientation of the player icons in the group chat HUD",
                        "Valid values are 'VERTICAL' and 'HORIZONTAL'"
                );
        groupPlayerIconPosX = builder
                .integerEntry("group_player_icon_pos_x", 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
                        "The X position of the player icons in the group chat HUD",
                        "Negative values mean anchoring to the right instead"
                );
        groupPlayerIconPosY = builder
                .integerEntry("group_player_icon_pos_y", 4, Integer.MIN_VALUE, Integer.MAX_VALUE,
                        "The Y position of the player icons in the group chat HUD",
                        "Negative values mean anchoring to the bottom instead"
                );
        hudIconPosX = builder
                .integerEntry("hud_icon_pos_x", 16, Integer.MIN_VALUE, Integer.MAX_VALUE,
                        "The X position of the icons in the voice chat HUD",
                        "Negative values mean anchoring to the right instead"
                );
        hudIconPosY = builder
                .integerEntry("hud_icon_pos_y", -16, Integer.MIN_VALUE, Integer.MAX_VALUE,
                        "The Y position of the icons in the voice chat HUD",
                        "Negative values mean anchoring to the bottom instead"
                );
        hudIconScale = builder
                .doubleEntry("hud_icon_scale", 1D, 0.01D, 10D,
                        "The scale of the icons in the voice chat HUD, such as microphone or connection status"
                );
        recordingDestination = builder
                .stringEntry("recording_destination", "",
                        "The location where recordings should be saved",
                        "Leave blank to use the default location"
                );
        recordingQuality = builder
                .integerEntry("recording_quality", 2, 0, 9,
                        "The quality of the recorded voice chat audio",
                        "0 = highest quality, 9 = lowest quality"
                );
        denoiser = builder
                .booleanEntry("denoiser", false,
                        "If noise suppression should be enabled"
                );
        runLocalServer = builder
                .booleanEntry("run_local_server", true,
                        "If the voice chat should work in singleplayer or in worlds shared over LAN"
                );
        javaMicrophoneImplementation = builder
                .booleanEntry("java_microphone_implementation", true, // TODO Fix AL microphone
                        "Whether to use the Java implementation of microphone capture instead of OpenAL"
                );
        showFakePlayersDisconnected = builder
                .booleanEntry("show_fake_players_disconnected", false,
                        "If fake players should have the disconnected icon above their head"
                );
        offlinePlayerVolumeAdjustment = builder
                .booleanEntry("offline_player_volume_adjustment", false,
                        "If the volume adjustment interface should also display offline players"
                );
        audioType = builder
                .enumEntry("audio_type", AudioType.NORMAL,
                        "The 3D audio type",
                        "Valid values are 'NORMAL', 'REDUCED', and 'OFF'"
                );
        useNatives = builder
                .booleanEntry("use_natives", true,
                        "If the mod should load native libraries",
                        "When disabled, the Java Opus implementation will be used instead, the denoiser won't be available, and you won't be able to record the voice chat audio"
                );
        freecamMode = builder
                .enumEntry("freecam_mode", FreecamMode.CAMERA,
                        "How listening to other players should work when using freecam mods",
                        "Valid values are 'CAMERA' and 'PLAYER'",
                        "CAMERA: You will hear the voice chat around your camera. Whether you will still be able to hear the voice chat when the camera is far away from your character depends on the voice chat broadcast range of the server",
                        "PLAYER: You will hear the voice chat around your character no matter where your camera is"
                );
        muteOnJoin = builder
                .booleanEntry("mute_on_join", false,
                        "If enabled, you will be automatically muted when joining a world"
                );


        if (!javaMicrophoneImplementation.get()) {
            javaMicrophoneImplementation.set(true).save();
        }

        if (Platform.isMac() && useNatives.get() && !VersionCheck.isMacOSNativeCompatible()) {
            useNatives.set(false).save();
        }
    }

}
