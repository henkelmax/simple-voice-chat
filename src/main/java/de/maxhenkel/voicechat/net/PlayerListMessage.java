package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.PlayerInfo;
import de.maxhenkel.voicechat.gui.AdjustVolumeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerListMessage implements Message<PlayerListMessage> {

    private List<PlayerInfo> players;

    public PlayerListMessage(List<PlayerInfo> players) {
        this.players = players;
    }

    public PlayerListMessage() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        openGUI();
    }

    @OnlyIn(Dist.CLIENT)
    private void openGUI() {
        Minecraft.getInstance().displayGuiScreen(new AdjustVolumeScreen(players));
    }

    @Override
    public PlayerListMessage fromBytes(PacketBuffer buf) {
        int count = buf.readInt();
        players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new PlayerInfo(buf.readUniqueId(), buf.readTextComponent()));
        }

        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(players.size());
        for (PlayerInfo info : players) {
            buf.writeUniqueId(info.getUuid());
            buf.writeTextComponent(info.getName());
        }
    }
}
