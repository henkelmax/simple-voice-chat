package de.maxhenkel.voicechat.integration.viaversion;

import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.compatibility.Compatibility1_12;
import net.kyori.adventure.key.Key;

public class ViaVersionCompatibility {

    public static void register() {
        for (String id : Voicechat.netManager.getPackets()) {
            Key key = Key.key(id);
            Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().put(String.format("%s:%s", Compatibility1_12.CHANNEL, key.value()), String.format("%s:%s", Voicechat.MODID, key.value()));
        }
    }

}
