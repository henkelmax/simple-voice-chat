package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.BuildConstants;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.plugins.impl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public abstract class MinecraftCompatibilityManager extends CommonCompatibilityManager {

    @Override
    public void registerCommands() {
        onRegisterServerCommands(VoicechatCommands::register);
    }

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands);

    @Override
    public VoicechatServerApi getServerApi() {
        return VoicechatServerApiImpl.INSTANCE;
    }

    @Override
    public void sendMinecraftPacket(ServerPlayer player, String id, VCByteBuf buffer) {
        MinecraftCompatibilityManager.getServerPlayer(player).connection.send(new ClientboundCustomPayloadPacket(new ResourceLocation(Voicechat.MODID, id), new FriendlyByteBuf(MinecraftCompatibilityManager.getFriendlyByteBuf(buffer))));
    }

    @Override
    public void displayClientMessage(ServerPlayer player, String message, boolean overlay) {
        MinecraftCompatibilityManager.getServerPlayer(player).displayClientMessage(Component.translatable(message), overlay);
    }

    @Override
    public void createTimeoutTimer(ServerPlayer player) {
        net.minecraft.server.level.ServerPlayer serverPlayer = MinecraftCompatibilityManager.getServerPlayer(player);
        Timer timer = new Timer("%s-login-timer".formatted(serverPlayer.getName()), true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                timer.purge();
                if (!serverPlayer.server.isRunning()) {
                    return;
                }
                if (!serverPlayer.connection.isAcceptingMessages()) {
                    return;
                }
                if (!Voicechat.SERVER.isCompatible(player)) {
                    executeOnMinecraftServer(MinecraftCompatibilityManager.fromServer(serverPlayer.server), () -> {
                        serverPlayer.connection.disconnect(
                                Component.literal(Voicechat.TRANSLATIONS.forceVoicechatKickMessage.get().formatted(
                                        CommonCompatibilityManager.INSTANCE.getModName(),
                                        CommonCompatibilityManager.INSTANCE.getModVersion()
                                )));
                    });
                }
            }
        }, Voicechat.SERVER_CONFIG.loginTimeout.get());
    }

    @Override
    public void executeOnMinecraftServer(MinecraftServer server, Runnable runnable) {
        MinecraftCompatibilityManager.getServer(server).execute(runnable);
    }

    @Override
    public void sendIncompatibleMessage(ServerPlayer serverPlayer, int compatibilityVersion) {
        Component component;
        if (compatibilityVersion <= 6) {
            component = Component.literal(Voicechat.TRANSLATIONS.voicechatNotCompatibleMessage.get().formatted(BuildConstants.MOD_COMPATIBLE_VERSION, CommonCompatibilityManager.INSTANCE.getModName()));
        } else {
            component = Component.translatableWithFallback("message.voicechat.incompatible_version",
                    "Your voice chat client version is not compatible with the server-side version.\nPlease install version %s of %s.",
                    Component.literal(BuildConstants.MOD_COMPATIBLE_VERSION).withStyle(ChatFormatting.BOLD),
                    Component.literal(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
        MinecraftCompatibilityManager.getServerPlayer(serverPlayer).sendSystemMessage(component);
    }

    public static Component getCategoryDisplayName(VolumeCategory category) {
        if (category.getNameTranslationKey() != null) {
            return Component.translatableWithFallback(category.getNameTranslationKey(), category.getName());
        }
        return Component.literal(category.getName());
    }

    public static String getCategorySearchName(VolumeCategory category) {
        if (category.getNameTranslationKey() == null) {
            return category.getName();
        }
        Language lang = Language.getInstance();
        return lang.getOrDefault(category.getNameTranslationKey(), category.getName());
    }

    public static Component getCategoryDisplayDescription(VolumeCategory category) {
        if (category.getDescriptionTranslationKey() != null) {
            return Component.translatableWithFallback(category.getDescriptionTranslationKey(), category.getDescription());
        }
        return category.getDescription() != null ? Component.literal(category.getDescription()) : Component.empty();
    }

    public static ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof net.minecraft.server.level.ServerLevel l) {
            return new ServerLevelImpl(l);
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of ServerLevel");
        }
    }

    public static ServerPlayer fromServerPlayer(Object serverPlayer) {
        if (serverPlayer instanceof net.minecraft.server.level.ServerPlayer p) {
            return new ServerPlayerImpl(p);
        } else {
            throw new IllegalArgumentException("serverPlayer is not an instance of ServerPlayer");
        }
    }

    public static MinecraftServer fromServer(Object minecraftServer) {
        if (minecraftServer instanceof net.minecraft.server.MinecraftServer s) {
            return new MinecraftServerImpl(s);
        } else {
            throw new IllegalArgumentException("minecraftServer is not an instance of MinecraftServer");
        }
    }

    @Override
    public Position createPosition(double x, double y, double z) {
        return fromVec3(new Vec3(x,y,z));
    }

    public static Position fromVec3(Object vec3) {
        if (vec3 instanceof Vec3 v) {
            return new PositionImpl(v);
        } else {
            throw new IllegalArgumentException("vec3 is not an instance of Vec3");
        }
    }

    @Override
    public VCByteBuf createVCByteBuff(Object byteBuf) {
        if (byteBuf instanceof ByteBuf b) {
            return new VCByteBufImpl(b);
        } else {
            throw new IllegalArgumentException("byteBuf is not an instance of ByteBuf");
        }
    }

    public static net.minecraft.server.level.ServerPlayer getServerPlayer(ServerPlayer player) {
        if (player.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            return serverPlayer;
        } else throw new IllegalArgumentException("player does not wrap ServerPlayer");
    }

    public static net.minecraft.server.MinecraftServer getServer(MinecraftServer server) {
        if (server.getMinecraftServer() instanceof net.minecraft.server.MinecraftServer minecraftServer) {
            return minecraftServer;
        } else throw new IllegalArgumentException("minecraftServer does not wrap MinecraftServer");
    }

    public static Vec3 getVec3(Position position) {
        if (position.getVec3() instanceof Vec3 vec3) {
            return vec3;
        } else throw new IllegalArgumentException("position does not wrap Vec3");
    }

    public static FriendlyByteBuf getFriendlyByteBuf(VCByteBuf vcByteBuf) {
        if (vcByteBuf.getBuffer() instanceof FriendlyByteBuf vec3) {
            return vec3;
        } else throw new IllegalArgumentException("vcByteBuf does not wrap FriendlyByteBuf");
    }

}
