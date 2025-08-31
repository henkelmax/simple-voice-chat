package de.maxhenkel.voicechat.integration.viaversion;

import de.maxhenkel.voicechat.VoiceProxy;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViaVersionCompatibility {

    private static final String LEGACY_CHANNEL = "vc";
    private static final String[] CHANNELS = {
            "update_state",
            "request_secret",
            "create_group",
            "set_group",
            "leave_group",
            "secret",
            "states",
            "state",
            "remove_state",
            "add_group",
            "remove_group",
            "joined_group",
            "add_category",
            "remove_category"
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
        for (String channel : CHANNELS) {
            put.invoke(mappingMap, String.format("%s:%s", LEGACY_CHANNEL, channel), String.format("%s:%s", VoiceProxy.MOD_ID, channel));
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