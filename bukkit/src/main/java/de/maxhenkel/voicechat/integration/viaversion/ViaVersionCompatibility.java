package de.maxhenkel.voicechat.integration.viaversion;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.compatibility.Compatibility1_12;
import net.kyori.adventure.key.Key;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViaVersionCompatibility {

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
        for (String id : Voicechat.netManager.getPackets()) {
            Key key = Key.key(id);
            put.invoke(mappingMap, String.format("%s:%s", Compatibility1_12.CHANNEL, key.value()), String.format("%s:%s", Voicechat.MODID, key.value()));
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
