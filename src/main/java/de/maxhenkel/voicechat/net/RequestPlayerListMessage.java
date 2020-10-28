package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.PlayerInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.stream.Collectors;

public class RequestPlayerListMessage implements Message<RequestPlayerListMessage> {

    public RequestPlayerListMessage() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Main.SIMPLE_CHANNEL.reply(new PlayerListMessage(context.getSender().server
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(playerEntity -> !playerEntity.getUniqueID().equals(context.getSender().getUniqueID()))
                .map(playerEntity -> new PlayerInfo(playerEntity.getUniqueID(), playerEntity.getDisplayName()))
                .collect(Collectors.toList())), context);
    }

    @Override
    public RequestPlayerListMessage fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
    }
}
