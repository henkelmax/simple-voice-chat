package de.maxhenkel.voicechat.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import javax.annotation.Nullable;

public class ClientServerChannel<T extends Packet<T>> extends Channel<T> {

    @Nullable
    private ClientServerNetManager.ClientReceiver<T> clientListener;

    public void setClientListener(ClientServerNetManager.ClientReceiver<T> packetReceiver) {
        clientListener = packetReceiver;
    }

    public void onClientPacket(LocalPlayer player, T packet) {
        Minecraft.getInstance().execute(() -> {
            if (clientListener != null) {
                clientListener.onPacket(player, packet);
            }
        });
    }

}
