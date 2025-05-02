package de.maxhenkel.voicechat.integration.viaversion;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViaVersionCompatibility {
    // TODO: make these class members like they are on bukkit normally.
    private static final String CHANNEL = "vc";
    private static final String MODID = "voicechat";

    // TODO: Somehow retrieve from elsewhere in the project the list of plugin channels required.
    private static final String[] Channels = {
        "remove_category",
        "leave_group",
        "create_group",
        "remove_group",
        "set_group",
        "player_states",
        "joined_group",
        "update_state",
        "add_group",
        "request_secret",
        "secret",
        "player_state",
        "add_category"
    };

    public static void register() throws Exception {
        // ViaVersion 4.x.x
        Class<?> protocol113to1122 = getClass("com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2");
        if (protocol113to1122 != null) {
            registerPackets(protocol113to1122);
            return;
        }
        //ViaVersion 5.x.x
        Class<?> protocol1122to113 = getClass("com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13");
        if (protocol1122to113 != null) {
            registerPackets(protocol1122to113);
            return;
        }
    }

    private static void registerPackets(Class<?> protocolClass) throws Exception {
        Field mappingsField = protocolClass.getDeclaredField("MAPPINGS");
        Object mappings = mappingsField.get(null);
        Method getChannelMappings = mappings.getClass().getDeclaredMethod("getChannelMappings");
        Object mappingMap = getChannelMappings.invoke(mappings);
        Method put = mappingMap.getClass().getDeclaredMethod("put", Object.class, Object.class);
        for (int i = 0; i < Channels.length; i++) {
            //System.out.println("Remapping " + Channels[i]);
            put.invoke(mappingMap, String.format("%s:%s", CHANNEL, Channels[i]), String.format("%s:%s", MODID, Channels[i]));
        }
    }

    @Nullable
    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}