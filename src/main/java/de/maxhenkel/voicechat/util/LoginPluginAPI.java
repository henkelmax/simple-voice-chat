package de.maxhenkel.voicechat.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.login.PacketLoginInCustomPayload;
import net.minecraft.network.protocol.login.PacketLoginOutCustomPayload;
import net.minecraft.resources.MinecraftKey;

import javax.annotation.Nullable;

public class LoginPluginAPI {
    private static final int PACKET_ID = 8712712;

    @Nullable
    public static PacketContainer generatePluginRequest(com.comphenix.protocol.wrappers.MinecraftKey channel, byte[] data) {
        MinecraftKey channelID = new MinecraftKey(channel.getPrefix(), channel.getKey());
        PacketDataSerializer tempBuffer = new PacketDataSerializer(Unpooled.buffer());
        tempBuffer.writeBytes(data);
        try {
            PacketLoginOutCustomPayload rawPacketHandle = new PacketLoginOutCustomPayload(PACKET_ID, channelID, tempBuffer);

            return new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD, rawPacketHandle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static FriendlyByteBuf readPluginResponse(PacketEvent event) {
        PacketLoginInCustomPayload pluginResponsePacket = (PacketLoginInCustomPayload) event.getPacket().getHandle();
        int id = pluginResponsePacket.b();
        PacketDataSerializer buf = pluginResponsePacket.c();
        if (buf == null && id == PACKET_ID) {
            event.setCancelled(true);
            return null;
        }
        if (id == PACKET_ID) {
            event.setCancelled(true);
            return new FriendlyByteBuf(buf);
        }
        return null;
    }

}
