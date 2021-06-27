package de.maxhenkel.voicechat.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MinecraftKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class LoginPluginAPI {
    private Class packetLoginOutCustomPayload;
    private Class packetDataSerializer;

    private static LoginPluginAPI INSTANCE = null;
    private static final int PACKET_ID = 8712712;

    private LoginPluginAPI() {

        PacketContainer packet = new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD);

        Object rawPacket = packet.getHandle();

        for (Field d : rawPacket.getClass().getDeclaredFields()) {
            d.setAccessible(true);
            try {
                Object o = d.get(rawPacket);
                if (o instanceof ByteBuf) {
                    packetDataSerializer = o.getClass();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        packetLoginOutCustomPayload = rawPacket.getClass();
    }

    @Nullable
    public PacketContainer generatePluginRequest(MinecraftKey channel, byte[] data) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        tempBuffer.writeVarInt(PACKET_ID);
        tempBuffer.writeUtf(channel.getFullKey());
        tempBuffer.writeBytes(data);

        try {
            Object serializer = packetDataSerializer.getDeclaredConstructor(ByteBuf.class).newInstance(tempBuffer.getUnderlyingByteBuf());
            Object rawPacketHandle = packetLoginOutCustomPayload.getDeclaredConstructor(this.packetDataSerializer).newInstance(serializer);
            return new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD, rawPacketHandle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public FriendlyByteBuf readPluginResponse(PacketEvent event) {
        Object pluginResponsePacket = event.getPacket().getHandle();
        ByteBuf pluginResponseBytes = null;
        int pluginResponseID = -1;

        for (Field d : pluginResponsePacket.getClass().getDeclaredFields()) {
            d.setAccessible(true);
            try {
                Object o = d.get(pluginResponsePacket);
                if (o instanceof ByteBuf) {
                    pluginResponseBytes = ((ByteBuf) o).copy();
                } else if (o instanceof Integer) {
                    if (!java.lang.reflect.Modifier.isStatic(d.getModifiers())) {
                        pluginResponseID = (Integer) o;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (pluginResponseBytes == null && pluginResponseID == PACKET_ID) {
            // Do nothing for vanilla clients
            event.setCancelled(true);
            return null;
        }
        if (pluginResponseID == PACKET_ID) {
            pluginResponseBytes.resetReaderIndex();
            byte[] bytes = new byte[pluginResponseBytes.readableBytes()];
            pluginResponseBytes.getBytes(0, bytes);
            event.setCancelled(true);
            return new FriendlyByteBuf(pluginResponseBytes);
        }
        return null;
    }

    public static LoginPluginAPI instance() {
        if (INSTANCE == null) {
            INSTANCE = new LoginPluginAPI();
        }
        return INSTANCE;
    }
}
