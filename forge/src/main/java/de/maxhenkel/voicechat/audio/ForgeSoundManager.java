package de.maxhenkel.voicechat.audio;

import com.sonicether.soundphysics.SoundPhysics;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.SpeakerException;
import de.maxhenkel.voicechat.voice.common.NamedThreadPoolFactory;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgeSoundManager extends SoundManager {

    private boolean soundPhysicsLoaded;
    private final ExecutorService executor;

    public ForgeSoundManager(@Nullable String deviceName) throws SpeakerException {
        super(deviceName);
        executor = Executors.newSingleThreadExecutor(NamedThreadPoolFactory.create("VoiceChatSoundPhysicsThread"));
        if (!VoicechatClient.CLIENT_CONFIG.soundPhysics.get()) {
            return;
        }
        if (ModList.get().isLoaded("sound_physics_remastered")) {
            try {
                Class.forName("com.sonicether.soundphysics.SoundPhysics");
                initSoundPhysics();
                soundPhysicsLoaded = true;
                Voicechat.LOGGER.info("Successfully initialized Sound Physics Remastered");
            } catch (Exception e) {
                Voicechat.LOGGER.warn("Failed to load Sound Physics Remastered: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void initSoundPhysics() {
        runInContext(executor, SoundPhysics::init);
    }

    public boolean isSoundPhysicsLoaded() {
        return soundPhysicsLoaded;
    }

    @Override
    public void close() {
        super.close();
        executor.shutdown();
    }
}
