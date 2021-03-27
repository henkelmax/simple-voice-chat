package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.PlayerInfo;
import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PlayerListPacket implements Packet<PlayerListPacket> {

    public static final Identifier PLAYER_LIST = new Identifier(Voicechat.MODID, "player_list");

    private List<PlayerInfo> players;

    public PlayerListPacket() {

    }

    public PlayerListPacket(List<PlayerInfo> players) {
        this.players = players;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    @Override
    public Identifier getID() {
        return PLAYER_LIST;
    }

    @Override
    public PlayerListPacket fromBytes(PacketByteBuf buf) {
        players = new ArrayList<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            players.add(new PlayerInfo(buf.readUuid(), buf.readText()));
        }
        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(players.size());
        for (PlayerInfo info : players) {
            buf.writeUuid(info.getUuid());
            buf.writeText(info.getName());
        }
    }

}
