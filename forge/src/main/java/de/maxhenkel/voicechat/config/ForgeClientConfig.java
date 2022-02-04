package de.maxhenkel.voicechat.config;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraftforge.common.ForgeConfigSpec;

import static de.maxhenkel.voicechat.config.ForgeServerConfig.wrapConfigEntry;

public class ForgeClientConfig extends ClientConfig {

    public ForgeClientConfig(ForgeConfigSpec.Builder builder) {
        microphoneActivationType = wrapConfigEntry(builder
                .comment("Microphone activation type")
                .defineEnum("microphone_activation_type", MicrophoneActivationType.PTT)
        );
        voiceActivationThreshold = wrapConfigEntry(builder
                .comment("The threshold for voice activation in dB")
                .defineInRange("voice_activation_threshold", -50D, -127D, 0D)
        );
        voiceChatVolume = wrapConfigEntry(builder
                .comment("The voice chat volume")
                .defineInRange("voice_chat_volume", 1D, 0D, 2D)
        );
        microphoneAmplification = wrapConfigEntry(builder
                .comment("The voice chat microphone amplification")
                .defineInRange("microphone_amplification", 1D, 0D, 4D)
        );
        outputBufferSize = wrapConfigEntry(builder
                .worldRestart()
                .comment(
                        "The size of the audio output buffer in packets",
                        "Higher values mean a higher latency, but less crackles",
                        "Increase this value if you have an unstable internet connection"
                )
                .defineInRange("output_buffer_size", 5, 1, 16)
        );
        audioPacketThreshold = wrapConfigEntry(builder
                .worldRestart()
                .comment(
                        "The maximum amount of audio packets that are held back, if a packet arrives out of order or gets dropped",
                        "This prevents discarding audio packets that are slightly out of order",
                        "Set this to 0 to disable"
                )
                .defineInRange("audio_packet_threshold", 3, 0, 16)
        );
        deactivationDelay = wrapConfigEntry(builder
                .comment(
                        "The time it takes for the microphone to deactivate when using voice activation",
                        "A value of 1 means 20 milliseconds, 2=40 ms, 3=60 ms, ..."
                )
                .defineInRange("voice_deactivation_delay", 25, 0, 100)
        );
        microphone = wrapConfigEntry(builder
                .worldRestart()
                .comment("The microphone used by the voice chat", "Empty for default device")
                .define("microphone", "")
        );
        speaker = wrapConfigEntry(builder
                .worldRestart()
                .comment("The speaker used by the voice chat", "Empty for default device")
                .define("speaker", "")
        );
        muted = wrapConfigEntry(builder
                .comment("If the microphone is muted (only when using voice activation)")
                .define("muted", false)
        );
        disabled = wrapConfigEntry(builder
                .comment("If the voice chat is disabled (sound and microphone off)")
                .define("disabled", false)
        );
        stereo = wrapConfigEntry(builder
                .worldRestart()
                .comment("If the voice chat should use semi 3D stereo sound")
                .define("stereo", true)
        );
        hideIcons = wrapConfigEntry(builder
                .comment("If the voice chat icons should be hidden")
                .define("hide_icons", false)
        );
        showGroupHUD = wrapConfigEntry(builder
                .comment("If the group HUD should be visible")
                .define("show_group_hud", true)
        );
        showOwnGroupIcon = wrapConfigEntry(builder
                .comment("If the own icon should be shown when in a group")
                .define("show_own_group_icon", true)
        );
        groupHudIconScale = wrapConfigEntry(builder
                .comment("The scale of the group HUD")
                .defineInRange("group_hud_icon_scale", 2D, 0.01D, 10D)
        );
        groupPlayerIconOrientation = wrapConfigEntry(builder
                .comment("The orientation of the player icons in the group HUD")
                .defineEnum("group_player_icon_orientation", GroupPlayerIconOrientation.VERTICAL)
        );
        groupPlayerIconPosX = wrapConfigEntry(builder
                .comment(
                        "The X position of the player icons in the group HUD",
                        "Negative values mean anchoring to the right"
                )
                .defineInRange("group_player_icon_pos_x", 4, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
        groupPlayerIconPosY = wrapConfigEntry(builder
                .comment(
                        "The Y position of the player icons in the group HUD",
                        "Negative values mean anchoring to the bottom"
                )
                .defineInRange("group_player_icon_pos_y", 4, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
        hudIconPosX = wrapConfigEntry(builder
                .comment(
                        "The X position of the HUD icons",
                        "Negative values mean anchoring to the right"
                )
                .defineInRange("hud_icon_pos_x", 16, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
        hudIconPosY = wrapConfigEntry(builder
                .comment(
                        "The Y position of the HUD icons",
                        "Negative values mean anchoring to the bottom"
                )
                .defineInRange("hud_icon_pos_y", -16, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
        hudIconScale = wrapConfigEntry(builder
                .comment("The scale of the HUD icons")
                .defineInRange("hud_icon_scale", 1D, 0.01D, 10D)
        );
        recordingDestination = wrapConfigEntry(builder
                .comment("The location where recordings should be saved", "Leave empty for default location")
                .define("recording_destination", "")
        );
        denoiser = wrapConfigEntry(builder
                .comment("If noise cancellation should be enabled")
                .define("denoiser", false)
        );
        soundPhysics = wrapConfigEntry(builder
                .comment("If sound physics integration should be enabled")
                .define("soundphysics", true)
        );
        runLocalServer = wrapConfigEntry(builder
                .comment("If voice chat should work in singleplayer/LAN worlds")
                .define("run_local_server", true)
        );
        javaMicrophoneImplementation = wrapConfigEntry(builder
                .comment("Whether to use the Java implementation of microphone capturing instead of OpenAL")
                .define("java_microphone_implementation", Platform.isMac())
        );
        macosMicrophoneWorkaround = wrapConfigEntry(builder
                .comment("If the microphone workaround hack should be used (MacOS only)")
                .define("macos_microphone_workaround", Platform.isMac())
        );
        showFakePlayersDisconnected = wrapConfigEntry(builder
                .comment("If fake players should have the disconnected icon above their head")
                .define("show_fake_players_disconnected", false)
        );
    }

}
