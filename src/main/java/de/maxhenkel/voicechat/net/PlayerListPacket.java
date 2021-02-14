package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.PlayerInfo;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PlayerListPacket {

    private List<PlayerInfo> players;

    public PlayerListPacket(List<PlayerInfo> players) {
        this.players = players;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public static PlayerListPacket fromBytes(PacketByteBuf buf) {
        int count = buf.readInt();
        List<PlayerInfo> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new PlayerInfo(buf.readUuid(), buf.readText()));
        }

        return new PlayerListPacket(players);
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(players.size());
        for (PlayerInfo info : players) {
            buf.writeUuid(info.getUuid());
            buf.writeText(info.getName());
        }
    }

}
