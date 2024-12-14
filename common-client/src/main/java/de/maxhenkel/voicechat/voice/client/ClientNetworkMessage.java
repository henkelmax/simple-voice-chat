package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ClientNetworkMessage {

    @Nullable
    public static NetworkMessage readPacketClient(RawUdpPacket packet, ClientVoicechatConnection client) throws IllegalAccessException, InstantiationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvocationTargetException, NoSuchMethodException {
        byte[] data = packet.getData();
        PacketBuffer b = new PacketBuffer(Unpooled.wrappedBuffer(data));
        if (b.readByte() != NetworkMessage.MAGIC_BYTE) {
            Voicechat.LOGGER.debug("Received invalid packet from {}", client.getAddress());
            return null;
        }
        return NetworkMessage.readFromBytes(packet.getSocketAddress(), client.getData().getSecret(), b.readByteArray(), System.currentTimeMillis());
    }

    public static byte[] writeClient(ClientVoicechatConnection client, NetworkMessage networkMessage) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] payload = networkMessage.write(client.getData().getSecret());
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(1 + 16 + payload.length));
        buffer.writeByte(NetworkMessage.MAGIC_BYTE);
        buffer.writeUUID(client.getData().getPlayerUUID());
        buffer.writeByteArray(payload);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

}
