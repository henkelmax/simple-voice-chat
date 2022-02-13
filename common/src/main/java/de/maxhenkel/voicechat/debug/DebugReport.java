package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.io.FileUtils;
import org.lwjgl.openal.AL11;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class DebugReport {

    private static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static final SimpleDateFormat TEXT_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final StringBuilder builder;

    private DebugReport() {
        builder = new StringBuilder();
    }

    public static void generateReport(PlayerEntity player) {
        try {
            Path path = generateReport();
            player.sendMessage(new TranslationTextComponent("message.voicechat.saved_debug_report",
                    new StringTextComponent(path.normalize().toString())
                            .withStyle(TextFormatting.GRAY)
                            .withStyle(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("message.voicechat.open")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.normalize().toString())))
            ), Util.NIL_UUID);
        } catch (IOException e) {
            player.sendMessage(new TranslationTextComponent("message.voicechat.saved_debug_report_failed", e.getMessage()), Util.NIL_UUID);
            e.printStackTrace();
        }
    }

    public static Path generateReport() throws IOException {
        DebugReport report = new DebugReport();
        report.generate();
        Path debugFolder = report.getDebugFolder();
        debugFolder.toFile().mkdirs();
        Path location = debugFolder.resolve("voicechat-" + FILE_FORMAT.format(Calendar.getInstance().getTime()) + ".txt");
        FileUtils.writeStringToFile(location.toFile(), report.builder.toString(), StandardCharsets.UTF_8);
        Voicechat.LOGGER.info("Saved voicechat debug report to {}", location.normalize().toString());
        return location;
    }

    private Path getDebugFolder() {
        return CommonCompatibilityManager.INSTANCE.getGameDirectory().resolve("debug");
    }

    private void generate() {
        appendHeader();
        divider();
        appendMods();
        divider();
        appendKeyBinds();
        divider();
        appendMics();
        divider();
        appendSpeakers();
        divider();
        appendOS();
        divider();
        appendJava();
        divider();
        appendOpenAL();
        divider();
        appendServer();
        divider();
        appendConfig();
        divider();
        appendPlayerVolumes();
    }

    private void appendHeader() {
        addLine("Simple Voice Chat Debug Report");
        addLine(TEXT_FORMAT.format(Calendar.getInstance().getTime()));
        addLine("Compatibility version " + Voicechat.COMPATIBILITY_VERSION);
        addLine("");
    }

    private void appendMods() {
        addLine("Loaded mods");
        addLine("");
        addLine(CommonCompatibilityManager.INSTANCE.listLoadedMods());
    }

    private void appendKeyBinds() {
        addLine("Keybinds");
        addLine("");
        addLine(CommonCompatibilityManager.INSTANCE.listKeybinds());
    }

    private void appendMics() {
        addLine("Input Devices");
        addLine("");
        MicrophoneManager.deviceNames().forEach(this::addLine);
        addLine("");
    }

    private void appendSpeakers() {
        addLine("Output Devices");
        addLine("");
        SoundManager.getAllSpeakers().forEach(this::addLine);
        addLine("");
    }

    private void appendOS() {
        addLine("Operating System");
        addLine(System.getProperty("os.name"));
        addLine(System.getProperty("os.version"));
        addLine(System.getProperty("os.arch"));
        addLine("");
    }

    private void appendJava() {
        addLine("Java");
        addLine("");
        addLine("Version: " + System.getProperty("java.version"));
    }

    private void appendOpenAL() {
        addLine("OpenAL");
        addLine("");
        addLine("Version: " + AL11.alGetString(AL11.AL_VERSION));
        addLine("Vendor: " + AL11.alGetString(AL11.AL_VENDOR));
    }

    private void appendServer() {
        addLine("Connection");
        Minecraft mc = Minecraft.getInstance();
        addLine(mc.isLocalServer() ? "Local Server" : "Dedicated Server");

        if (!mc.isLocalServer()) {
            try {
                SocketAddress socketAddress = ClientCompatibilityManager.INSTANCE.getSocketAddress(mc.getConnection().getConnection());
                addLine("Server address: " + socketAddress.toString());
            } catch (Exception e) {
                addLine("Server address: N/A (" + e.getMessage() + ")");
            }
        }
        ClientVoicechat client = ClientManager.getClient();
        if (client != null && client.getConnection() != null) {
            ClientVoicechatConnection connection = client.getConnection();
            addLine("");
            addLine("Voice chat connected");
            addLine("Address: " + connection.getAddress().toString());
            addLine("Port: " + connection.getData().getServerPort());
            addLine("Codec: " + connection.getData().getCodec().toString());
            addLine(connection.getData().groupsEnabled() ? "Groups enabled" : "Groups disabled");
            addLine("Sample rate: " + SoundManager.SAMPLE_RATE);
            addLine("Frame size: " + SoundManager.FRAME_SIZE);
            addLine("MTU size: " + connection.getData().getMtuSize());
            addLine("Distance: " + connection.getData().getVoiceChatDistance());
            addLine("Fade distance: " + connection.getData().getVoiceChatFadeDistance());
            addLine("Crouch distance multiplier: " + connection.getData().getCrouchDistanceMultiplier());
            addLine("Whisper distance multiplier: " + connection.getData().getWhisperDistanceMultiplier());
            addLine("Authenticated: " + connection.isAuthenticated());
            addLine("Recording: " + (client.getRecorder() != null));
            addLine("");
        } else {
            addLine("");
            addLine("Voice chat not connected");
            addLine("");
        }
    }

    private void appendConfig() {
        addLine("Client Configuration");
        addLine("");
        for (Map.Entry<String, Object> o : VoicechatClient.CLIENT_CONFIG.hideIcons.getConfig().getEntries().entrySet()) {
            addLine(o.getKey() + ": " + o.getValue());
        }
        addLine("");
    }

    private void appendPlayerVolumes() {
        addLine("Player volumes");
        addLine("");

        for (Map.Entry<String, Object> o : VoicechatClient.VOLUME_CONFIG.getEntries().entrySet()) {
            addLine(o.getKey() + ": " + o.getValue());
        }
        addLine("");
    }

    private void divider() {
        addLine("#####################################################################################################\n");
    }

    private void addLine(String str) {
        builder.append(str).append("\n");
    }

}
