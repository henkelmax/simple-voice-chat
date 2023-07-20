package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SecretPacket implements Packet<SecretPacket> {

    public static final Key SECRET = Voicechat.compatibility.createNamespacedKey("secret");

    private UUID secret;
    private int serverPort;
    private UUID playerUUID;
    private ServerConfig.Codec codec;
    private int mtuSize;
    private double voiceChatDistance;
    private int keepAlive;
    private boolean groupsEnabled;
    private String voiceHost;
    private boolean allowRecording;

    public SecretPacket() {

    }

    public SecretPacket(Player player, UUID secret, int port, ServerConfig serverConfig) {
        this.secret = secret;
        this.serverPort = port;
        this.playerUUID = player.getUniqueId();
        this.codec = serverConfig.voiceChatCodec.get();
        this.mtuSize = serverConfig.voiceChatMtuSize.get();
        this.voiceChatDistance = serverConfig.voiceChatDistance.get();
        this.keepAlive = serverConfig.keepAlive.get();
        this.groupsEnabled = serverConfig.groupsEnabled.get();
        this.voiceHost = PluginManager.instance().getVoiceHost(serverConfig.voiceHost.get());
        this.allowRecording = serverConfig.allowRecording.get();
    }

    public UUID getSecret() {
        return secret;
    }

    public int getServerPort() {
        return serverPort;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ServerConfig.Codec getCodec() {
        return codec;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public String getVoiceHost() {
        return voiceHost;
    }

    @Override
    public Key getID() {
        return SECRET;
    }

    public boolean allowRecording() {
        return allowRecording;
    }

    @Override
    public SecretPacket fromBytes(FriendlyByteBuf buf) {
        secret = buf.readUUID();
        serverPort = buf.readInt();
        playerUUID = buf.readUUID();
        codec = ServerConfig.Codec.values()[buf.readByte()];
        mtuSize = buf.readInt();
        voiceChatDistance = buf.readDouble();
        keepAlive = buf.readInt();
        groupsEnabled = buf.readBoolean();
        voiceHost = buf.readUtf();
        allowRecording = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(secret);
        buf.writeInt(serverPort);
        buf.writeUUID(playerUUID);
        buf.writeByte(codec.ordinal());
        buf.writeInt(mtuSize);
        buf.writeDouble(voiceChatDistance);
        buf.writeInt(keepAlive);
        buf.writeBoolean(groupsEnabled);
        buf.writeUtf(voiceHost);
        buf.writeBoolean(allowRecording);
    }

}
