package de.maxhenkel.voicechat.config;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;

public class FabricClientConfig extends ClientConfig {

    public FabricClientConfig(ConfigBuilder builder) {
        voiceChatVolume = builder.doubleEntry("voice_chat_volume", 1D, 0D, 2D);
        voiceActivationThreshold = builder.doubleEntry("voice_activation_threshold", -50D, -127D, 0D);
        microphoneAmplification = builder.doubleEntry("microphone_amplification", 1D, 0D, 4D);
        microphoneActivationType = builder.enumEntry("microphone_activation_type", MicrophoneActivationType.PTT);
        outputBufferSize = builder.integerEntry("output_buffer_size", 5, 1, 16);
        audioPacketThreshold = builder.integerEntry("audio_packet_threshold", 3, 0, 16);
        deactivationDelay = builder.integerEntry("voice_deactivation_delay", 25, 0, 100);
        microphone = builder.stringEntry("microphone", "");
        speaker = builder.stringEntry("speaker", "");
        muted = builder.booleanEntry("muted", false);
        disabled = builder.booleanEntry("disabled", false);
        stereo = builder.booleanEntry("stereo", true);
        hideIcons = builder.booleanEntry("hide_icons", false);
        showGroupHUD = builder.booleanEntry("show_group_hud", true);
        showOwnGroupIcon = builder.booleanEntry("show_own_group_icon", true);
        groupHudIconScale = builder.doubleEntry("group_hud_icon_scale", 2D, 0.01D, 10D);
        groupPlayerIconOrientation = builder.enumEntry("group_player_icon_orientation", GroupPlayerIconOrientation.VERTICAL);
        groupPlayerIconPosX = builder.integerEntry("group_player_icon_pos_x", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
        groupPlayerIconPosY = builder.integerEntry("group_player_icon_pos_y", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
        hudIconPosX = builder.integerEntry("hud_icon_pos_x", 16, Integer.MIN_VALUE, Integer.MAX_VALUE);
        hudIconPosY = builder.integerEntry("hud_icon_pos_y", -16, Integer.MIN_VALUE, Integer.MAX_VALUE);
        hudIconScale = builder.doubleEntry("hud_icon_scale", 1D, 0.01D, 10D);
        recordingDestination = builder.stringEntry("recording_destination", "");
        denoiser = builder.booleanEntry("denoiser", false);
        soundPhysics = builder.booleanEntry("soundphysics", true);
        runLocalServer = builder.booleanEntry("run_local_server", true);
        javaMicrophoneImplementation = builder.booleanEntry("java_microphone_implementation", Platform.isMac());
        macosMicrophoneWorkaround = builder.booleanEntry("macos_microphone_workaround", true);
        showFakePlayersDisconnected = builder.booleanEntry("show_fake_players_disconnected", false);
    }

}
