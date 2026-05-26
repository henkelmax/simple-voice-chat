package de.maxhenkel.voicechat.voice.rtc;
/*

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import dev.onvoid.webrtc.*;
import io.netty.channel.Channel;
import net.minecraft.client.network.webrtc.RtcChannel;
import net.minecraft.client.network.webrtc.RtcHandshake;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class WebRTCVoicechatClientSocketImpl implements ClientVoicechatSocket {

    private final LinkedBlockingQueue<VoicechatWebrtcRawUdpPacket> queue;
    private final WebRtcSocketAddress socketAddress;
    @Nullable
    private RTCDataChannel dataChannel;

    public WebRTCVoicechatClientSocketImpl() {
        queue = new LinkedBlockingQueue<>();
        socketAddress = new WebRtcSocketAddress();
    }

    @Override
    public void open() throws Exception {
        Channel channel = WebRtcUtils.getClientChannel();
        if (channel == null) {
            throw new IllegalStateException("Client channel not found");
        }
        if (!(channel instanceof RtcChannel rtcChannel)) {
            throw new IllegalStateException("Client channel is not an instance of RtcChannel");
        }
        RtcHandshake.HandshakeResult handshakeResult = WebRtcUtils.getHandshakeResult(rtcChannel);
        dataChannel = handshakeResult.peerConnection().createDataChannel(Voicechat.MODID, WebRtcUtils.createInit());
        dataChannel.registerObserver(new RTCDataChannelObserver() {
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
                queue.add(new VoicechatWebrtcRawUdpPacket(data, System.currentTimeMillis(), socketAddress));
                if (queue.size() > 100) {
                    CooldownTimer.run("voicechat_webrtc_queue_overflow", () -> {
                        Voicechat.LOGGER.warn("WebRTC packet queue is overflowing ({} packets)", queue.size());
                    });
                }
            }
        });
    }

    @Override
    public RawUdpPacket read() throws Exception {
        return queue.take();
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        if (dataChannel != null) {
            dataChannel.send(new RTCDataChannelBuffer(ByteBuffer.wrap(data), true));
        }
    }

    @Override
    public void close() {
        if (dataChannel != null) {
            dataChannel.close();
            dataChannel = null;
        }
    }

    @Override
    public boolean isClosed() {
        return dataChannel == null;
    }
}
*/
