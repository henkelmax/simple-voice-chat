package de.maxhenkel.voicechat.integration.viaversion;

import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.compatibility.Compatibility1_12;

public class ViaVersionCompatibility {

    public static void register() {
        for (String id : Voicechat.netManager.getPackets()) {
            Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().put(String.format("%s:%s", Compatibility1_12.CHANNEL, id.split(":")[1]), id);
            Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().put(id, String.format("%s:%s", Voicechat.MODID, id.split(":")[1]));
        }
    }

}
