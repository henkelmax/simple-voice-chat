package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface Message<T> {
    void executeServerSide(NetworkEvent.Context context);

    void executeClientSide(NetworkEvent.Context context);

    T fromBytes(PacketBuffer buf);

    void toBytes(PacketBuffer buf);


}
