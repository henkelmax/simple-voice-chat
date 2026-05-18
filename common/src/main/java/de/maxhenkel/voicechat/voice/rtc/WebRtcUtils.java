package de.maxhenkel.voicechat.voice.rtc;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.Server;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCPriorityType;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.network.webrtc.RtcChannel;
import net.minecraft.client.network.webrtc.RtcHandshake;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class WebRtcUtils {

    public static final int ID = 69;

    public static RTCDataChannelInit createInit() {
        RTCDataChannelInit rtcDataChannelInit = new RTCDataChannelInit();
        rtcDataChannelInit.maxRetransmits = 0;
        rtcDataChannelInit.ordered = false;
        rtcDataChannelInit.priority = RTCPriorityType.MEDIUM;
        rtcDataChannelInit.negotiated = true;
        rtcDataChannelInit.id = ID;
        return rtcDataChannelInit;
    }

    private static Field CHANNEL_FIELD;

    private static Channel getChannel(Connection connection) throws Exception {
        if (CHANNEL_FIELD == null) {
            CHANNEL_FIELD = Connection.class.getDeclaredField("channel");
            CHANNEL_FIELD.setAccessible(true);
        }
        return (Channel) CHANNEL_FIELD.get(connection);
    }

    @Nullable
    public static Channel getClientChannel() throws Exception {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }
        return getChannel(connection.getConnection());
    }

    public static boolean isConnectedViaWebRtc() {
        try {
            return getClientChannel() instanceof RtcChannel;
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to check connection channel type", e);
            return false;
        }
    }

    private static Field CONNECTION_FIELD;

    private static Connection getConnection(ServerCommonPacketListenerImpl connection) throws Exception {
        if (CONNECTION_FIELD == null) {
            CONNECTION_FIELD = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            CONNECTION_FIELD.setAccessible(true);
        }
        return (Connection) CONNECTION_FIELD.get(connection);
    }

    private static Field HANDSHAKE_RESULT_FIELD;

    public static RtcHandshake.HandshakeResult getHandshakeResult(RtcChannel channel) throws Exception {
        if (HANDSHAKE_RESULT_FIELD == null) {
            HANDSHAKE_RESULT_FIELD = RtcChannel.class.getDeclaredField("handshakeResult");
            HANDSHAKE_RESULT_FIELD.setAccessible(true);
        }
        return (RtcHandshake.HandshakeResult) HANDSHAKE_RESULT_FIELD.get(channel);
    }

    /**
     * Adds an unchecked client WebRTC connection to the server in case this is being hosted on a client via WebRTC
     *
     * @param server the server
     * @param player the player
     */
    public static void addUncheckedRtcConnection(Server server, ServerPlayer player) {
        if (server.getServer().isDedicatedServer()) {
            return;
        }
        addUncheckedRtcConnection0(server, player);
    }

    /**
     * Adds an unchecked client WebRTC connection to the server.
     * Note that this method should only be called if not on a dedicated server.
     *
     * @param server the server
     * @param player the player
     */
    private static void addUncheckedRtcConnection0(Server server, ServerPlayer player) {
        try {
            Connection c = getConnection(player.connection);
            Channel channel = getChannel(c);
            if (channel instanceof RtcChannel rtcChannel) {
                server.addUncheckedPlayerConnection(player.getUUID(), new WebRtcClientConnection(player.getUUID(), getHandshakeResult(rtcChannel).peerConnection(), server));
            }
        } catch (Throwable t) {
            Voicechat.LOGGER.error("Failed to add RTC client connection", t);
        }
    }
}
