package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import javax.annotation.Nullable;

public class ClientServerChannel<T extends Packet<T>> extends Channel<T> {

    @Nullable
    private ClientServerNetManager.ClientReceiver<T> clientListener;

    public void setClientListener(ClientServerNetManager.ClientReceiver<T> packetReceiver) {
        clientListener = packetReceiver;
    }

    public void onClientPacket(Minecraft client, ClientPacketListener handler, T packet) {
        client.execute(() -> {
            if (clientListener != null) {
                clientListener.onPacket(client, handler, packet);
            }
        });
    }

}
