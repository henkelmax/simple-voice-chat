package de.maxhenkel.voicechat.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketLoginInCustomPayload;
import net.minecraft.server.v1_16_R3.PacketLoginOutCustomPayload;

import javax.annotation.Nullable;
import java.io.IOException;

public class LoginPluginAPI {
    private static final int PACKET_ID = 8712712;

    @Nullable
    public static PacketContainer generatePluginRequest(com.comphenix.protocol.wrappers.MinecraftKey channel, byte[] data) {
        MinecraftKey channelID = new MinecraftKey(channel.getPrefix(), channel.getKey());
        PacketDataSerializer tempBuffer = new PacketDataSerializer(Unpooled.buffer());
        tempBuffer.d(PACKET_ID);
        tempBuffer.a(channelID);
        tempBuffer.writeBytes(data);
        try {
            PacketLoginOutCustomPayload rawPacketHandle = new PacketLoginOutCustomPayload();
            rawPacketHandle.a(tempBuffer);

            return new PacketContainer(PacketType.Login.Server.CUSTOM_PAYLOAD, rawPacketHandle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static FriendlyByteBuf readPluginResponse(PacketEvent event) {
        PacketLoginInCustomPayload pluginResponsePacket = (PacketLoginInCustomPayload) event.getPacket().getHandle();
        PacketDataSerializer pluginResponseData = new PacketDataSerializer(Unpooled.buffer());
        try {
            pluginResponsePacket.b(pluginResponseData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int id = pluginResponseData.i();
        Boolean understood = pluginResponseData.readBoolean();
        ByteBuf buf = pluginResponseData.readBytes(pluginResponseData.readableBytes());

        if (!understood && id == PACKET_ID) {
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
