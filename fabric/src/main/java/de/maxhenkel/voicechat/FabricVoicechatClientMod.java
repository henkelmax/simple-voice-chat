package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class FabricVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        initializeClient();
        ClientLoginNetworking.registerGlobalReceiver(FabricVoicechatMod.INIT, (client, handler, buf, listenerAdder) -> {
            int serverCompatibilityVersion = buf.readInt();

            if (serverCompatibilityVersion != FabricVoicechatMod.COMPATIBILITY_VERSION) {
                FabricVoicechatMod.LOGGER.warn("Incompatible voice chat version (server={}, client={})", serverCompatibilityVersion, FabricVoicechatMod.COMPATIBILITY_VERSION);
            }

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeInt(FabricVoicechatMod.COMPATIBILITY_VERSION);
            return CompletableFuture.completedFuture(buffer);
        });
    }


    @Override
    public ClientCompatibilityManager createCompatibilityManager() {
        return new FabricClientCompatibilityManager();
    }
}
