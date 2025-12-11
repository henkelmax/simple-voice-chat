package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VCByteBuf;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.impl.VCByteBufImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

import java.util.Timer;
import java.util.TimerTask;

public class OutSourcingImpl implements OutSourcing {
    @Override
    public TimerTask getTimerSchedule(ServerVoiceEvents voiceEvents, Timer timer, Server server, ServerPlayer player) {
        net.minecraft.server.level.ServerPlayer serverPlayer = (net.minecraft.server.level.ServerPlayer) player.getPlayer();
        return new TimerTask() {
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
                if (!voiceEvents.isCompatible(player)) {
                    CommonCompatibilityManager.INSTANCE.execute(getServerApi().fromServer(serverPlayer.server), () -> {
                        serverPlayer.connection.disconnect(
                                Component.literal(Voicechat.TRANSLATIONS.forceVoicechatKickMessage.get().formatted(
                                        CommonCompatibilityManager.INSTANCE.getModName(),
                                        CommonCompatibilityManager.INSTANCE.getModVersion()
                                )));
                    });
                }
            }
        };
    }

    @Override
    public void setServerListener(ServerVoiceEvents serverVoiceEvents) {
        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.setServerListener((server, player, handler, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {} ({})", player.getName(), packet.getCompatibilityVersion());
            serverVoiceEvents.clientCompatibilities.put(player.getUuid(), packet.getCompatibilityVersion());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                ((net.minecraft.server.level.ServerPlayer)player.getPlayer()).sendSystemMessage(getIncompatibleMessage(packet.getCompatibilityVersion()));
            } else {
                serverVoiceEvents.initializePlayerConnection(player);
            }
        });
    }

    @Override
    public void sendCustomPacket(ServerPlayer player, String id, VCByteBuf buffer) {
        ((net.minecraft.server.level.ServerPlayer)player.getPlayer()).connection.send(new ClientboundCustomPayloadPacket(new ResourceLocation(Voicechat.MODID, id), new FriendlyByteBuf((ByteBuf) buffer.getBuffer())));
    }

    @Override
    public boolean hasGroupPermissions(ServerPlayer player) {
        if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
            ((net.minecraft.server.level.ServerPlayer) player.getPlayer()).displayClientMessage(Component.translatable("message.voicechat.no_group_permission"), true);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSpeakPermissions(ServerPlayer player) {
        net.minecraft.server.level.ServerPlayer serverPlayer = ((net.minecraft.server.level.ServerPlayer) player.getPlayer());
        if (!PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player)) {
            CooldownTimer.run("no-speak-" + player.getUuid(), 30_000L, () -> {
                serverPlayer.displayClientMessage(Component.translatable("message.voicechat.no_speak_permission"), true);
            });
            return false;
        }
        return true;
    }

    @Override
    public boolean hasListenPermissions(ServerPlayer player) {
        net.minecraft.server.level.ServerPlayer serverPlayer = ((net.minecraft.server.level.ServerPlayer) player.getPlayer());
        if (!PermissionManager.INSTANCE.LISTEN_PERMISSION.hasPermission(player)) {
            CooldownTimer.run(String.format("no-listen-%s", player.getUuid()), 30_000L, () -> {
                serverPlayer.displayClientMessage(Component.translatable("message.voicechat.no_listen_permission"), true);
            });
            return false;
        }
        return true;
    }

    @Override
    public void serverExecute(MinecraftServer server, Runnable runnable) {
        ((net.minecraft.server.MinecraftServer) server.getMinecraftServer()).execute(runnable);
    }

    @Override
    public void registercommands() {
        ((CommonCompatibilityManager)CommonCompatibilityManager.INSTANCE).onRegisterServerCommands(VoicechatCommands::register);
    }

    @Override
    public VoicechatServerApi getServerApi() {
        return VoicechatServerApiImpl.INSTANCE;
    }

    @Override
    public VCByteBuf byteBufOf(ByteBuf byteBuf) {
        return new VCByteBufImpl(byteBuf);
    }

    public Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return Component.literal(Voicechat.TRANSLATIONS.voicechatNotCompatibleMessage.get().formatted(BuildConstants.MOD_COMPATIBLE_VERSION, CommonCompatibilityManager.INSTANCE.getModName()));
        } else {
            return Component.translatableWithFallback("message.voicechat.incompatible_version",
                    "Your voice chat client version is not compatible with the server-side version.\nPlease install version %s of %s.",
                    Component.literal(BuildConstants.MOD_COMPATIBLE_VERSION).withStyle(ChatFormatting.BOLD),
                    Component.literal(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
    }
}
