package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

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
        Main.LOGGER.info("Saved voicechat debug report to {}", location.normalize().toString());
        return location;
    }

    private Path getDebugFolder() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("debug");
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
        appendServer();
        divider();
        // appendConfig();
        // divider();
        appendPlayerVolumes();
    }

    private void appendHeader() {
        addLine("Simple Voice Chat Debug Report");
        addLine(TEXT_FORMAT.format(Calendar.getInstance().getTime()));
        addLine("");
    }

    private void appendMods() {
        addLine("Loaded mods");
        addLine("");

        for (ModInfo mod : ModList.get().getMods()) {
            addLine("Mod ID: " + mod.getModId());
            addLine("Name: " + mod.getDisplayName());
            addLine("Version: " + mod.getVersion().getQualifier());
            addLine("Dependencies: " + mod.getDependencies().stream().map(IModInfo.ModVersion::getModId).collect(Collectors.joining(", ")));
            addLine("");
        }
    }

    private void appendKeyBinds() {
        addLine("Keybinds");
        addLine("");
        for (KeyBinding mapping : Minecraft.getInstance().options.keyMappings) {
            addLine(mapping.getName() + "(" + mapping.getCategory() + "): " + mapping.getKey().getName() + " (" + mapping.getDefaultKey().getName() + ")");
        }
        addLine("");
    }

    private void appendMics() {
        addLine("Input Devices");
        addLine("");
        for (String mic : DataLines.getMicrophoneNames()) {
            TargetDataLine microphone = DataLines.getMicrophoneByName(mic);
            if (microphone == null) {
                addLine(mic + ": Not found");
                continue;
            }
            addLine(mic
                    + ": "
                    + microphone.getFormat().getSampleRate()
                    + " Hz "
                    + microphone.getFormat().getEncoding().toString()
                    + ", "
                    + microphone.getFormat().getFrameSize()
                    + " bytes, "
                    + microphone.getFormat().getChannels()
                    + " channels, "
                    + (microphone.getFormat().isBigEndian() ? "BE" : "LE")
            );
        }
        addLine("");
    }

    private void appendSpeakers() {
        addLine("Output Devices");
        addLine("");
        for (String name : DataLines.getSpeakerNames()) {
            SourceDataLine speaker = DataLines.getSpeakerByName(name);
            if (speaker == null) {
                addLine(name + ": Not found");
                continue;
            }
            addLine(name
                    + ": "
                    + speaker.getFormat().getSampleRate()
                    + " Hz "
                    + speaker.getFormat().getEncoding().toString()
                    + ", "
                    + speaker.getFormat().getFrameSize()
                    + " bytes, "
                    + speaker.getFormat().getChannels()
                    + " channels, "
                    + (speaker.getFormat().isBigEndian() ? "BE" : "LE")
            );
        }
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
        addLine("");
    }

    private void appendServer() {
        addLine("Connection");
        Minecraft mc = Minecraft.getInstance();
        addLine(mc.isLocalServer() ? "Local Server" : "Dedicated Server");

        if (!mc.isLocalServer()) {
            try {
                SocketAddress socketAddress = mc.getConnection().getConnection().channel().remoteAddress();
                addLine("Server address: " + socketAddress.toString());
            } catch (Exception e) {
                addLine("Server address: N/A (" + e.getMessage() + ")");
            }
        }
        Client client = Main.CLIENT_VOICE_EVENTS.getClient();
        if (client != null) {
            addLine("");
            addLine("Voice chat connected");
            addLine("Address: " + client.getAddress().toString());
            addLine("Port: " + client.getPort());
            addLine("Codec: " + Main.SERVER_CONFIG.voiceChatCodec.get().toString());
            addLine(Main.SERVER_CONFIG.groupsEnabled.get() ? "Groups enabled" : "Groups disabled");
            addLine("Sample rate: " + AudioChannelConfig.getSampleRate());
            addLine("Frame size: " + AudioChannelConfig.getFrameSize());
            addLine("MTU size: " + Main.SERVER_CONFIG.voiceChatMtuSize.get());
            addLine("Distance: " + Main.SERVER_CONFIG.voiceChatDistance.get());
            addLine("Fade distance: " + Main.SERVER_CONFIG.voiceChatFadeDistance.get());
            addLine("Authenticated: " + client.isAuthenticated());
            addLine("Recording: " + (client.getRecorder() != null));
            addLine("");
        } else {
            addLine("");
            addLine("Voice chat not connected");
            addLine("");
        }
    }

    // TODO add config values
    /*private void appendConfig() {
        addLine("Client Configuration");
        addLine("");
        for (UnmodifiableConfig.Entry o : Main.CLIENT_CONFIG.getConfigSpec().entrySet()) {
            addLine(o.getKey() + ": " + o.getValue());
        }
        addLine("");
    }*/

    private void appendPlayerVolumes() {
        addLine("Player volumes");
        addLine("");

        for (Map.Entry<Object, Object> o : Main.VOLUME_CONFIG.getProperties().entrySet()) {
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
