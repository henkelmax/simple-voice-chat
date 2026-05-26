package de.maxhenkel.voicechat.voice.rtc;
/*

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import dev.onvoid.webrtc.*;

import java.nio.ByteBuffer;
import java.util.UUID;

public class WebRtcClientConnection extends ClientConnection {

    private final RTCDataChannel rtcDataChannel;

    public WebRtcClientConnection(UUID playerUUID, RTCPeerConnection peerConnection, Server server) {
        super(playerUUID, new WebRtcSocketAddress());
        rtcDataChannel = peerConnection.createDataChannel(Voicechat.MODID, WebRtcUtils.createInit());
        rtcDataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long previousAmount) {
            }

            @Override
            public void onStateChange() {
            }

            @Override
            public void onMessage(RTCDataChannelBuffer buffer) {
                byte[] data = new byte[buffer.data.remaining()];
                buffer.data.get(data);
                server.addRawPacket(new VoicechatWebrtcRawUdpPacket(data, System.currentTimeMillis(), address));
            }
        });
    }

    @Override
    public void send(Server server, NetworkMessage message) throws Exception {
        rtcDataChannel.send(new RTCDataChannelBuffer(ByteBuffer.wrap(message.writeServer(server, this)), true));
    }

}
*/
