package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClientReceiveSoundEventImpl extends ClientEventImpl implements ClientReceiveSoundEvent {

    private UUID id;
    private short[] rawAudio;

    public ClientReceiveSoundEventImpl(VoicechatClientApi api, UUID id, short[] rawAudio) {
        super(api);
        this.id = id;
        this.rawAudio = rawAudio;

    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Nullable
    @Override
    public short[] getRawAudio() {
        return rawAudio;
    }

    @Override
    public void setRawAudio(@Nullable short[] rawAudio) {
        this.rawAudio = rawAudio;
    }

    public static class EntitySoundImpl extends ClientReceiveSoundEventImpl implements ClientReceiveSoundEvent.EntitySound {
        private boolean whispering;

        public EntitySoundImpl(VoicechatClientApi api, UUID id, short[] rawAudio, boolean whispering) {
            super(api, id, rawAudio);
            this.whispering = whispering;
        }

        @Override
        public boolean isWhispering() {
            return whispering;
        }
    }

    public static class LocationalSoundImpl extends ClientReceiveSoundEventImpl implements ClientReceiveSoundEvent.LocationalSound {
        private Position position;

        public LocationalSoundImpl(VoicechatClientApi api, UUID id, short[] rawAudio, Position position) {
            super(api, id, rawAudio);
            this.position = position;
        }

        @Override
        public Position getPosition() {
            return position;
        }

    }

    public static class StaticSoundImpl extends ClientReceiveSoundEventImpl implements ClientReceiveSoundEvent.StaticSound {

        public StaticSoundImpl(VoicechatClientApi api, UUID id, short[] rawAudio) {
            super(api, id, rawAudio);
        }

    }

}
