package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.DebugOverlay;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.macos.PermissionCheck;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.macos.avfoundation.AVAuthorizationStatus;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.channel.local.LocalAddress;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientManager {

    @Nullable
    private ClientVoicechat client;
    private final ClientPlayerStateManager playerStateManager;
    private final ClientGroupManager groupManager;
    private final ClientCategoryManager categoryManager;
    private final PTTKeyHandler pttKeyHandler;
    private final RenderEvents renderEvents;
    private final DebugOverlay debugOverlay;
    private final KeyEvents keyEvents;
    private final Minecraft minecraft;
    private boolean hasShownPermissionsMessage;

    private ClientManager() {
        playerStateManager = new ClientPlayerStateManager();
        groupManager = new ClientGroupManager();
        categoryManager = new ClientCategoryManager();
        pttKeyHandler = new PTTKeyHandler();
        renderEvents = new RenderEvents();
        debugOverlay = new DebugOverlay();
        keyEvents = new KeyEvents();
        minecraft = Minecraft.getInstance();

        ClientCompatibilityManager.INSTANCE.onJoinWorld(this::onJoinWorld);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
        ClientCompatibilityManager.INSTANCE.onPublishServer(this::onPublishServer);

        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(connection -> {
            if (client != null) {
                client.onVoiceChatConnected(connection);
            }
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(() -> {
            if (client != null) {
                client.onVoiceChatDisconnected();
            }
        });

        CommonCompatibilityManager.INSTANCE.getNetManager().secretChannel.setClientListener((client, handler, packet) -> authenticate(packet));
    }

    private void authenticate(SecretPacket secretPacket) {
        if (client == null) {
            Voicechat.LOGGER.error("Received secret without a client being present");
            return;
        }
        Voicechat.LOGGER.info("Received secret");
        if (client.getConnection() != null) {
            ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
        }
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            try {
                SocketAddress socketAddress = ClientCompatibilityManager.INSTANCE.getSocketAddress(connection.getConnection());
                if (socketAddress instanceof InetSocketAddress address) {
                    client.connect(new InitializationData(address.getHostString(), secretPacket));
                } else if (socketAddress instanceof LocalAddress) {
                    client.connect(new InitializationData("127.0.0.1", secretPacket));
                }
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to determine server address", e);
            }
        }
    }

    private void onJoinWorld() {
        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            VoicechatClient.CLIENT_CONFIG.muted.set(true);
        }
        if (client != null) {
            Voicechat.LOGGER.info("Disconnecting from previous connection due to server change");
            onDisconnect();
        }
        hasShownPermissionsMessage = false;
        Voicechat.LOGGER.info("Sending secret request to the server");
        NetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        client = new ClientVoicechat();
    }

    public void checkMicrophonePermissions() {
        if (!VoicechatClient.CLIENT_CONFIG.macosCheckMicrophonePermission.get()) {
            return;
        }
        if (VersionCheck.isMacOSNativeCompatible()) {
            AVAuthorizationStatus status = PermissionCheck.getMicrophonePermissions();
            if (status.equals(AVAuthorizationStatus.DENIED)) {
                if (!hasShownPermissionsMessage) {
                    ClientManager.sendPlayerError("message.voicechat.macos_no_mic_permission", null);
                    hasShownPermissionsMessage = true;
                }
                Voicechat.LOGGER.warn("User hasn't granted microphone permissions: {}", status.name());
            } else if (!status.equals(AVAuthorizationStatus.AUTHORIZED)) {
                if (!hasShownPermissionsMessage) {
                    ClientManager.sendPlayerError("message.voicechat.macos_unsupported_launcher", null);
                    hasShownPermissionsMessage = true;
                }
                Voicechat.LOGGER.warn("User has an unsupported launcher: {}", status.name());
            }
        }
    }

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        sendPlayerMessage(
                ComponentUtils.wrapInSquareBrackets(new TextComponent(CommonCompatibilityManager.INSTANCE.getModName()))
                        .withStyle(ChatFormatting.GREEN)
                        .append(" ")
                        .append(new TranslatableComponent(translationKey).withStyle(ChatFormatting.RED))
                        .withStyle(style -> {
                            if (e != null) {
                                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(e.getMessage()).withStyle(ChatFormatting.RED)));
                            }
                            return style;
                        }));
    }

    public static void sendPlayerMessage(Component component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendMessage(component, Util.NIL_UUID);
    }

    private void onDisconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    private void onPublishServer(int port) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        try {
            Voicechat.LOGGER.info("Changing voice chat port to {}", port);
            server.changePort(port);
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                ClientVoicechatConnection connection = client.getConnection();
                if (connection != null) {
                    Voicechat.LOGGER.info("Force disconnecting due to port change");
                    connection.disconnect();
                }
            }
            NetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to change voice chat port", e);
        }
        Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("message.voicechat.server_port", server.getPort()));
    }

    @Nullable
    public static ClientVoicechat getClient() {
        return instance().client;
    }

    public static ClientPlayerStateManager getPlayerStateManager() {
        return instance().playerStateManager;
    }

    public static ClientGroupManager getGroupManager() {
        return instance().groupManager;
    }

    public static ClientCategoryManager getCategoryManager() {
        return instance().categoryManager;
    }

    public static PTTKeyHandler getPttKeyHandler() {
        return instance().pttKeyHandler;
    }

    public static RenderEvents getRenderEvents() {
        return instance().renderEvents;
    }

    public static DebugOverlay getDebugOverlay() {
        return instance().debugOverlay;
    }

    public KeyEvents getKeyEvents() {
        return keyEvents;
    }

    private static ClientManager instance;

    public static synchronized ClientManager instance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

}
