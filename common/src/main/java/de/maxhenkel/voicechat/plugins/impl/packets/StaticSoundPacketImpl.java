package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;

import javax.annotation.Nullable;
import java.util.UUID;

public class StaticSoundPacketImpl extends SoundPacketImpl implements StaticSoundPacket {

    public StaticSoundPacketImpl(GroupSoundPacket packet) {
        super(packet);
    }

    public static class BuilderImpl extends SoundPacketImpl.BuilderImpl<BuilderImpl, StaticSoundPacket> implements StaticSoundPacket.Builder<BuilderImpl> {

        public BuilderImpl(SoundPacketImpl soundPacket) {
            super(soundPacket);
        }

        public BuilderImpl(UUID sender, byte[] opusEncodedData, long sequenceNumber, @Nullable String category) {
            super(sender, opusEncodedData, sequenceNumber, category);
        }

        @Override
        public StaticSoundPacket build() {
            return new StaticSoundPacketImpl(new GroupSoundPacket(sender, opusEncodedData, sequenceNumber, category));
        }

    }

}
