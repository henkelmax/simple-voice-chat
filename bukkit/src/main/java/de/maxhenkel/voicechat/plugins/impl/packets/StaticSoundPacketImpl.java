package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;

public class StaticSoundPacketImpl extends SoundPacketImpl implements StaticSoundPacket {

    public StaticSoundPacketImpl(GroupSoundPacket packet) {
        super(packet);
    }

}
