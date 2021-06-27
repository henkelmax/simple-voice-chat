package de.maxhenkel.voicechat.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Random;

public class LoginPluginAPI {

    private Class packetLoginOutCustomPayload;
    private Class packetDataSerializer;
    private Random random;

    private LoginPluginAPI() {
        this.random = new Random();

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
        tempBuffer.writeVarInt(random.nextInt());
        tempBuffer.writeUtf(channel.getFullKey(), 32000);
        tempBuffer.writeBytes(data);

        try {
            Object packetDataSerializer = this.packetDataSerializer.getDeclaredConstructor(ByteBuf.class).newInstance(tempBuffer.getUnderlyingByteBuf());
            Object rawPacketHandle = packetLoginOutCustomPayload.getDeclaredConstructor(this.packetDataSerializer).newInstance(packetDataSerializer);
            return new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD, rawPacketHandle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public FriendlyByteBuf readPluginResponse(PacketContainer packet) {
        Object pluginResponsePacket = packet.getHandle();
        ByteBuf pluginResponseBytes = null;
        for (Field d : pluginResponsePacket.getClass().getDeclaredFields()) {
            d.setAccessible(true);
            try {
                Object o = d.get(pluginResponsePacket);
                if (o instanceof ByteBuf) {
                    pluginResponseBytes = ((ByteBuf) o).copy();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (pluginResponseBytes == null) {
            return null;
        }
        pluginResponseBytes.resetReaderIndex();
        byte[] bytes = new byte[pluginResponseBytes.readableBytes()];
        pluginResponseBytes.getBytes(0, bytes);

        return new FriendlyByteBuf(pluginResponseBytes);
    }

    private static LoginPluginAPI instance;

    public static LoginPluginAPI instance() {
        if (instance == null) {
            instance = new LoginPluginAPI();
        }
        return instance;
    }

}