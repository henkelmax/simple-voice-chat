package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.GroupScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import de.maxhenkel.voicechat.net.SecretPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class ClientManager {

    @Nullable
    private ClientVoicechat client;
    private final ClientPlayerStateManager playerStateManager;
    private final PTTKeyHandler pttKeyHandler;
    private final RenderEvents renderEvents;
    private final KeyEvents keyEvents;
    private final Minecraft minecraft;

    private ClientManager() {
        playerStateManager = new ClientPlayerStateManager();
        pttKeyHandler = new PTTKeyHandler();
        renderEvents = new RenderEvents();
        keyEvents = new KeyEvents();
        minecraft = Minecraft.getInstance();

        ClientCompatibilityManager.INSTANCE.onJoinServer(this::onJoinServer);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);

        CommonCompatibilityManager.INSTANCE.getNetManager().secretChannel.registerServerListener((client, handler, packet) -> {
            authenticate(handler.getLocalGameProfile().getId(), packet);
        });

        CommonCompatibilityManager.INSTANCE.getNetManager().setGroupChannel.registerServerListener((client, handler, packet) -> {
            String newGroup = packet.getGroup().isEmpty() ? null : packet.getGroup();
            if (newGroup == null && playerStateManager.getGroup() == null) {
                return;
            }
            if (newGroup != null && newGroup.equals(playerStateManager.getGroup())) {
                return;
            }
            playerStateManager.setGroup(newGroup);
            if (minecraft.screen instanceof GroupScreen || minecraft.screen instanceof CreateGroupScreen) {
                minecraft.setScreen(null);
            }
        });
    }

    private void authenticate(UUID playerUUID, SecretPacket secretPacket) {
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
                    String ip = secretPacket.getVoiceHost().isEmpty() ? address.getHostString() : secretPacket.getVoiceHost();
                    Voicechat.LOGGER.info("Connecting to server: '" + ip + ":" + secretPacket.getServerPort() + "'");
                    client.connect(new InitializationData(ip, playerUUID, secretPacket));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onJoinServer() {
        Voicechat.LOGGER.info("Sending secret request to the server");
        CommonCompatibilityManager.INSTANCE.getNetManager().sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        client = new ClientVoicechat();
    }

    private void onDisconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    @Nullable
    public static ClientVoicechat getClient() {
        return instance().client;
    }

    public static ClientPlayerStateManager getPlayerStateManager() {
        return instance().playerStateManager;
    }

    public static PTTKeyHandler getPttKeyHandler() {
        return instance().pttKeyHandler;
    }

    public static RenderEvents getRenderEvents() {
        return instance().renderEvents;
    }

    public KeyEvents getKeyEvents() {
        return keyEvents;
    }

    private static ClientManager instance;

    public static ClientManager instance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

}
