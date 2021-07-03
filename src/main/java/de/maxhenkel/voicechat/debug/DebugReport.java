package de.maxhenkel.voicechat.debug;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.IClientConnection;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyCodeAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugReport {

    private static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static final SimpleDateFormat TEXT_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final StringBuilder builder;

    private DebugReport() {
        builder = new StringBuilder();
    }

    public static void generateReport(Player player) {
        try {
            Path path = generateReport();
            player.sendMessage(new TranslatableComponent("message.voicechat.saved_debug_report",
                    new TextComponent(path.normalize().toString())
                            .withStyle(ChatFormatting.GRAY)
                            .withStyle(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("message.voicechat.open")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.normalize().toString())))
            ), Util.NIL_UUID);
        } catch (IOException e) {
            player.sendMessage(new TranslatableComponent("message.voicechat.saved_debug_report_failed", e.getMessage()), Util.NIL_UUID);
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
        return FabricLoader.getInstance().getGameDir().resolve("debug");
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
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = mod.getMetadata();
            addLine("Mod ID: " + metadata.getId());
            addLine("Name: " + metadata.getName());
            addLine("Version: " + metadata.getVersion());
            addLine("Dependencies: " + metadata.getDepends().stream().map(ModDependency::getModId).collect(Collectors.joining(", ")));
            addLine("");
        }
    }

    private void appendKeyBinds() {
        addLine("Fabric Keybinds");
        addLine("");
        try {
            Field moddedKeyBindings = KeyBindingRegistryImpl.class.getDeclaredField("moddedKeyBindings");
            moddedKeyBindings.setAccessible(true);
            List<KeyMapping> mappings = (List<KeyMapping>) moddedKeyBindings.get(null);
            for (KeyMapping mapping : mappings) {
                InputConstants.Key boundKey = ((KeyCodeAccessor) mapping).fabric_getBoundKey();
                addLine(mapping.getName() + "(" + mapping.getCategory() + "): " + boundKey.getName() + " (" + mapping.getDefaultKey().getName() + ")");
            }
            addLine("");
        } catch (Exception e) {
            addLine("Error: " + e.getMessage());
            addLine("");
        }
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
                SocketAddress socketAddress = ((IClientConnection) mc.getConnection().getConnection()).getChannel().remoteAddress();
                addLine("Server address: " + socketAddress.toString());
            } catch (Exception e) {
                addLine("Server address: N/A (" + e.getMessage() + ")");
            }
        }
        Client client = VoicechatClient.CLIENT.getClient();
        if (client != null) {
            addLine("");
            addLine("Voice chat connected");
            addLine("Address: " + client.getAddress().toString());
            addLine("Port: " + client.getPort());
            addLine("Codec: " + client.getCodec().toString());
            addLine(client.groupsEnabled() ? "Groups enabled" : "Groups disabled");
            addLine("Sample rate: " + client.getAudioChannelConfig().getSampleRate());
            addLine("Frame size: " + client.getAudioChannelConfig().getFrameSize());
            addLine("MTU size: " + client.getMtuSize());
            addLine("Distance: " + client.getVoiceChatDistance());
            addLine("Fade distance: " + client.getVoiceChatFadeDistance());
            addLine("Authenticated: " + client.isAuthenticated());
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
        for (Map.Entry<Object, Object> o : VoicechatClient.CLIENT_CONFIG.hideIcons.getConfig().getProperties().entrySet()) {
            addLine(o.getKey() + ": " + o.getValue());
        }
        addLine("");
    }

    private void appendPlayerVolumes() {
        addLine("Player volumes");
        addLine("");

        for (Map.Entry<Object, Object> o : VoicechatClient.VOLUME_CONFIG.getProperties().entrySet()) {
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
