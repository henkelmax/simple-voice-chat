package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricClientConfig;
import de.maxhenkel.voicechat.integration.ClothConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.FabricClientCompatibilityManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class FabricVoicechatClientMod extends VoicechatClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), true, FabricClientConfig::new);

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

        ClothConfig.init();
    }

    @Override
    public ClientCompatibilityManager createCompatibilityManager() {
        return new FabricClientCompatibilityManager();
    }
}
