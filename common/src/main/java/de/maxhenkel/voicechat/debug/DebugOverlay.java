package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.speaker.ALSpeaker;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.*;

public class DebugOverlay {

    private static final Minecraft mc = Minecraft.getInstance();

    private final Map<UUID, AudioChannelInfo> audioChannelInfoMap;
    private boolean active;
    @Nullable
    private TimerThread timer;

    public DebugOverlay() {
        audioChannelInfoMap = new LinkedHashMap<>();
        ClientCompatibilityManager.INSTANCE.onRenderHUD(this::render);
    }

    public void toggle() {
        active = !active;
        if (active) {
            audioChannelInfoMap.clear();
            timer = new TimerThread();
        } else {
            if (timer != null) {
                timer.close();
            }
            audioChannelInfoMap.clear();
        }
    }

    private List<String> rightText = new ArrayList<>();

    private void render(GuiGraphics gui, float tickDelta) {
        if (!active) {
            return;
        }
        rightText.clear();

        rightText.add(String.format("%s %s debug overlay", CommonCompatibilityManager.INSTANCE.getModName(), CommonCompatibilityManager.INSTANCE.getModVersion()));
        rightText.add(String.format("Press ALT + %s to toggle", ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getDisplayName().getString()));
        rightText.add(null);

        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            rightText.add("Voice chat not running");
            drawRight(gui, rightText);
            return;
        }
        rightText.add(getAudioChannelCountString());
        addAudioChannelStrings(rightText);

        drawRight(gui, rightText);
    }

    private String getAudioChannelCountString() {
        return String.format("Audio Channels: %s", audioChannelInfoMap.size());
    }

    public static final int MAX_AUDIO_CHANNELS = 4;

    private void addAudioChannelStrings(List<String> strings) {
        ArrayList<Map.Entry<UUID, AudioChannelInfo>> entries = new ArrayList<>(audioChannelInfoMap.entrySet());
        for (int i = 0; i < entries.size() && i < MAX_AUDIO_CHANNELS; i++) {
            Map.Entry<UUID, AudioChannelInfo> entry = entries.get(i);
            AudioChannelInfo audioChannel = entry.getValue();
            if (audioChannel.audioBufferCount < 0) {
                strings.add(String.format(
                        "ID: %s Packets: %s Reordering: %S Lost: %s Queue: STOPPED",
                        entry.getKey().toString().substring(24),
                        audioChannel.bufferedPackets,
                        audioChannel.packetReorderingBuffer,
                        audioChannel.lostPackets
                ));
            } else {
                strings.add(String.format(
                        "ID: %s Packets: %s Reordering: %S Lost: %s Queue: %s/%s",
                        entry.getKey().toString().substring(24),
                        audioChannel.bufferedPackets,
                        audioChannel.packetReorderingBuffer,
                        audioChannel.lostPackets,
                        audioChannel.audioBufferCount,
                        audioChannel.audioBufferSize
                ));
            }
        }
        if (entries.size() > 4) {
            strings.add(String.format("%s more channels", entries.size() - MAX_AUDIO_CHANNELS));
        }
    }

    private void updateCache() {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            audioChannelInfoMap.clear();
            return;
        }

        Map<UUID, AudioChannel> audioChannels = client.getAudioChannels();

        audioChannelInfoMap.values().removeIf(audioChannelInfo -> !audioChannels.containsKey(audioChannelInfo.id));

        for (Map.Entry<UUID, AudioChannel> entry : audioChannels.entrySet()) {
            AudioChannel audioChannel = entry.getValue();
            AudioChannelInfo info = audioChannelInfoMap.computeIfAbsent(entry.getKey(), uuid -> new AudioChannelInfo(entry.getKey()));
            info.update(audioChannel);
        }
    }

    private static final int LEFT_PADDING = 5;

    private void drawRight(GuiGraphics gui, List<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            String text = strings.get(i);
            if (text == null || text.isEmpty()) {
                continue;
            }
            gui.pose().pushPose();
            int width = mc.font.width(text);
            gui.pose().translate(mc.getWindow().getGuiScaledWidth() - width - LEFT_PADDING, 25F + i * (mc.font.lineHeight + 1F), 0F);
            gui.fill(-1, -1, width, mc.font.lineHeight, 0x90505050);
            gui.drawString(mc.font, text, 0, 0, 0xFFFFFF, false);

            gui.pose().popPose();
        }
    }

    private static class AudioChannelInfo {
        private final UUID id;
        private int audioBufferSize;
        private int audioBufferCount;
        private int bufferedPackets;
        private int packetReorderingBuffer;
        private long lostPackets;

        public AudioChannelInfo(UUID id) {
            this.id = id;
        }

        public AudioChannelInfo update(AudioChannel audioChannel) {
            audioBufferSize = 32;
            audioBufferCount = -1;
            bufferedPackets = audioChannel.getQueue().size();
            packetReorderingBuffer = audioChannel.getPacketBuffer().getSize();
            lostPackets = audioChannel.getLostPackets();

            Speaker speaker = audioChannel.getSpeaker();
            if (speaker instanceof ALSpeaker) {
                ((ALSpeaker) speaker).fetchQueuedBuffersAsync(bufferCount -> {
                    audioBufferCount = bufferCount;
                });
            }

            return this;
        }
    }

    private class TimerThread extends Thread {
        private boolean stopped;

        private TimerThread() {
            setName("Voicechat Debug Overlay Thread");
            setDaemon(true);
            setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
            start();
        }

        @Override
        public void run() {
            while (!stopped) {
                updateCache();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {
                }
            }
        }

        public void close() {
            stopped = true;
            interrupt();
        }

    }

}
