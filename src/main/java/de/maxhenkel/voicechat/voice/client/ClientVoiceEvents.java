package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.ClientWorldEvents;
import de.maxhenkel.voicechat.events.IClientConnection;
import de.maxhenkel.voicechat.events.RenderEvents;
import de.maxhenkel.voicechat.gui.AdjustVolumeScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.Packets;
import de.maxhenkel.voicechat.net.PlayerListPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientVoiceEvents {

    private static final Identifier MICROPHONE_ICON = new Identifier(Voicechat.MODID, "textures/gui/microphone.png");
    private static final Identifier MICROPHONE_MUTED_ICON = new Identifier(Voicechat.MODID, "textures/gui/microphone_muted.png");
    private static final Identifier SPEAKER_ICON = new Identifier(Voicechat.MODID, "textures/gui/speaker.png");

    private Client client;
    private MinecraftClient minecraft;

    public ClientVoiceEvents() {
        minecraft = MinecraftClient.getInstance();

        ClientWorldEvents.DISCONNECT.register(this::disconnect);

        HudRenderCallback.EVENT.register(this::renderHUD);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
        RenderEvents.RENDER_NAMEPLATE.register(this::onRenderName);

        ClientSidePacketRegistry.INSTANCE.register(Packets.SECRET, (packetContext, attachedData) -> {
            InitPacket initPacket = InitPacket.fromBytes(attachedData);
            packetContext.getTaskQueue().execute(() -> {
                authenticate(packetContext.getPlayer().getUuid(), initPacket);
            });
        });

        ClientSidePacketRegistry.INSTANCE.register(Packets.PLAYER_LIST, (packetContext, attachedData) -> {
            PlayerListPacket list = PlayerListPacket.fromBytes(attachedData);
            packetContext.getTaskQueue().execute(() -> {
                minecraft.openScreen(new AdjustVolumeScreen(list.getPlayers()));
            });
        });
    }

    public void authenticate(UUID playerUUID, InitPacket initPacket) {
        Voicechat.LOGGER.info("Received secret");
        if (client != null) {
            disconnect();
        }
        ClientPlayNetworkHandler connection = minecraft.getNetworkHandler();
        if (connection != null) {
            try {
                SocketAddress socketAddress = ((IClientConnection) connection.getConnection()).getChannel().remoteAddress();
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    String ip = address.getHostString();
                    Voicechat.LOGGER.info("Connecting to server: '" + ip + ":" + initPacket.getServerPort() + "'");
                    client = new Client(ip, initPacket.getServerPort(), playerUUID, initPacket.getSecret(), initPacket.getSampleRate(), initPacket.getMtuSize(), initPacket.getVoiceChatDistance(), initPacket.getVoiceChatFadeDistance(), initPacket.getKeepAlive());
                    client.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    public void renderHUD(MatrixStack stack, float tickDelta) {
        if (client == null || !client.isConnected() || client.getMicThread() == null) {
            return;
        }

        if (client.getMicThread().isTalking()) {
            renderIcon(stack, MICROPHONE_ICON);
        } else if (client.isMuted() && VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(stack, MICROPHONE_MUTED_ICON);
        }
    }

    private void renderIcon(MatrixStack matrixStack, Identifier texture) {
        matrixStack.push();
        minecraft.getTextureManager().bindTexture(texture);
        //double width = minecraft.getMainWindow().getScaledWidth();
        int height = minecraft.getWindow().getScaledHeight();
        Screen.drawTexture(matrixStack, 16, height - 32, 0, 0, 16, 16, 16, 16);
        matrixStack.pop();
    }

    public void onClientTickEnd(MinecraftClient minecraft) {
        if (VoicechatClient.KEY_VOICE_CHAT_SETTINGS.wasPressed()) {
            if (VoicechatClient.CLIENT.getClient() == null || !VoicechatClient.CLIENT.getClient().isAuthenticated()) {
                sendUnavailableMessage();
            } else {
                minecraft.openScreen(new VoiceChatScreen());
            }
        }

        if (VoicechatClient.KEY_PTT.isPressed()) {
            if (VoicechatClient.CLIENT.getClient() == null || !VoicechatClient.CLIENT.getClient().isAuthenticated()) {
                sendUnavailableMessage();
            }
        }

        if (VoicechatClient.KEY_MUTE.isPressed()) {
            Client client = VoicechatClient.CLIENT.getClient();
            if (client == null || !client.isAuthenticated()) {
                sendUnavailableMessage();
            } else {
                client.setMuted(!client.isMuted());
            }
        }
    }

    public void sendUnavailableMessage() {
        minecraft.player.sendMessage(new TranslatableText("message.voicechat.voice_chat_unavailable"), true);
    }

    public void onRenderName(Entity entity, MatrixStack stack, VertexConsumerProvider vertexConsumers, int light) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity playerEntity = (PlayerEntity) entity;
        if (client != null && client.getTalkCache().isTalking(playerEntity) && !minecraft.options.hudHidden) {
            renderSpeaker(playerEntity, entity.getDisplayName(), stack, vertexConsumers, light);
        }
    }

    protected void renderSpeaker(PlayerEntity player, Text displayNameIn, MatrixStack matrixStackIn, VertexConsumerProvider buffer, int light) {
        matrixStackIn.push();
        matrixStackIn.translate(0D, player.getHeight() + 0.5D, 0D);
        matrixStackIn.multiply(minecraft.getEntityRenderDispatcher().getRotation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.textRenderer.getWidth(displayNameIn) / 2 + 2);


        VertexConsumer builder = buffer.getBuffer(RenderLayer.getText(SPEAKER_ICON));
        int alpha = 32;

        if (player.isSneaky()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderLayer.getTextSeeThrough(SPEAKER_ICON));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        }

        matrixStackIn.pop();
    }

    private static void vertex(VertexConsumer builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }

    private static void vertex(VertexConsumer builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        MatrixStack.Entry entry = matrixStack.peek();
        builder.vertex(entry.getModel(), x, y, z)
                .color(255, 255, 255, alpha)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry.getNormal(), 0F, 0F, -1F)
                .next();
    }

}
