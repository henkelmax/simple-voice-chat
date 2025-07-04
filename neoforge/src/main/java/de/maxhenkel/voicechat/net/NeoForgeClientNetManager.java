package de.maxhenkel.voicechat.net;

import net.minecraft.client.player.LocalPlayer;

public class NeoForgeClientNetManager {

    static <T extends Packet<T>> void onClientPacket(LocalPlayer player, ClientServerChannel<T> channel, T packet) {
        channel.onClientPacket(player, packet);
    }

}
