package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.config.FabricServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.FabricCommonCompatibilityManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class FabricVoicechatMod extends Voicechat implements ModInitializer {

    public static final ResourceLocation INIT = new ResourceLocation(FabricVoicechatMod.MODID, "init");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), true, FabricServerConfig::new);
        });

        initialize();

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeInt(COMPATIBILITY_VERSION);
            sender.sendPacket(INIT, buffer);
        });
        ServerLoginNetworking.registerGlobalReceiver(INIT, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) {
                //Let vanilla clients pass, but not incompatible voice chat clients
                return;
            }

            int clientCompatibilityVersion = buf.readInt();

            if (clientCompatibilityVersion != FabricVoicechatMod.COMPATIBILITY_VERSION) {
                FabricVoicechatMod.LOGGER.warn("Client {} has incompatible voice chat version (server={}, client={})", handler.connection.getRemoteAddress(), FabricVoicechatMod.COMPATIBILITY_VERSION, clientCompatibilityVersion);
                handler.disconnect(SERVER.getIncompatibleMessage(clientCompatibilityVersion));
            }
        });

        CommonCompatibilityManager.INSTANCE.onRegisterServerCommands(VoicechatCommands::register);
    }

    @Override
    public int readCompatibilityVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (modContainer == null) {
            return -1;
        }
        return modContainer.getMetadata().getCustomValue(MODID).getAsObject().get("compatibilityVersion").getAsNumber().intValue();
    }

    @Override
    public CommonCompatibilityManager createCompatibilityManager() {
        return new FabricCommonCompatibilityManager();
    }

}
