package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class SetGroupMessage implements Message<SetGroupMessage> {

    private String group;

    public SetGroupMessage(String group) {
        this.group = group;
    }

    public SetGroupMessage() {

    }

    public String getGroup() {
        return group;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        exec();
    }

    @OnlyIn(Dist.CLIENT)
    private void exec() {
        Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().setGroup(group.isEmpty() ? null : group);
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public SetGroupMessage fromBytes(FriendlyByteBuf buf) {
        group = buf.readUtf(512);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(group, 512);
    }
}
