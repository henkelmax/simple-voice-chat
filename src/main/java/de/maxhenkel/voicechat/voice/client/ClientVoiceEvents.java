package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.event.VoiceChatConnectedEvent;
import de.maxhenkel.voicechat.event.VoiceChatDisconnectedEvent;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientVoiceEvents {

    private static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Main.MODID, "textures/gui/microphone.png");
    private static final ResourceLocation MICROPHONE_OFF_ICON = new ResourceLocation(Main.MODID, "textures/gui/microphone_off.png");
    private static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Main.MODID, "textures/gui/speaker.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Main.MODID, "textures/gui/speaker_off.png");
    private static final ResourceLocation DISCONNECT_ICON = new ResourceLocation(Main.MODID, "textures/gui/disconnected.png");

    private Client client;
    private ClientPlayerStateManager playerStateManager;
    private Minecraft minecraft;

    public ClientVoiceEvents() {
        playerStateManager = new ClientPlayerStateManager();
        minecraft = Minecraft.getInstance();
    }

    public void authenticate(UUID playerUUID, UUID secret) {
        Main.LOGGER.info("Received secret");
        if (client != null) {
            onDisconnect();
        }
        ClientPlayNetHandler connection = minecraft.getConnection();
        if (connection != null) {
            try {
                SocketAddress socketAddress = connection.getConnection().channel().remoteAddress();
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    String ip = address.getHostString();
                    Main.LOGGER.info("Connecting to server: '" + ip + ":" + Main.SERVER_CONFIG.voiceChatPort.get() + "'");
                    client = new Client(ip, Main.SERVER_CONFIG.voiceChatPort.get(), playerUUID, secret);
                    client.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void disconnectEvent(WorldEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.gameMode == null) {
            playerStateManager.onDisconnect();
            onDisconnect();
        }
    }

    @SubscribeEvent
    public void connectedEvent(VoiceChatConnectedEvent event) {
        playerStateManager.onVoiceChatConnected(event.getClient());
    }

    @SubscribeEvent
    public void disconnectedEvent(VoiceChatDisconnectedEvent event) {
        playerStateManager.onVoiceChatDisconnected();
    }

    public void onDisconnect() {
        MinecraftForge.EVENT_BUS.post(new VoiceChatDisconnectedEvent());
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Nullable
    public Client getClient() {
        return client;
    }

    public ClientPlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.CHAT)) {
            return;
        }

        if (Main.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }

        if (playerStateManager.isDisconnected()) {
            renderIcon(event.getMatrixStack(), DISCONNECT_ICON);
        } else if (playerStateManager.isDisabled()) {
            renderIcon(event.getMatrixStack(), SPEAKER_OFF_ICON);
        } else if (playerStateManager.isMuted() && Main.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(event.getMatrixStack(), MICROPHONE_OFF_ICON);
        } else if (client != null && client.getMicThread() != null && client.getMicThread().isTalking()) {
            renderIcon(event.getMatrixStack(), MICROPHONE_ICON);
        }
    }

    private void renderIcon(MatrixStack matrixStack, ResourceLocation texture) {
        matrixStack.pushPose();
        minecraft.getTextureManager().bind(texture);
        //double width = minecraft.getMainWindow().getScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        Screen.blit(matrixStack, 16, height - 32, 0, 0, 16, 16, 16, 16);
        matrixStack.popPose();
    }

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        if (Main.KEY_VOICE_CHAT_SETTINGS.consumeClick()) {
            if (Main.CLIENT_VOICE_EVENTS.getClient() == null || !Main.CLIENT_VOICE_EVENTS.getClient().isAuthenticated()) {
                sendUnavailableMessage();
            } else {
                minecraft.setScreen(new VoiceChatScreen());
            }
        }

        if (Main.KEY_PTT.consumeClick()) {
            if (Main.CLIENT_VOICE_EVENTS.getClient() == null || !Main.CLIENT_VOICE_EVENTS.getClient().isAuthenticated()) {
                sendUnavailableMessage();
            }
        }

        if (Main.KEY_MUTE.consumeClick()) {
            Client client = Main.CLIENT_VOICE_EVENTS.getClient();
            if (client == null || !client.isAuthenticated()) {
                sendUnavailableMessage();
            } else {
                playerStateManager.setMuted(!playerStateManager.isMuted());
            }
        }

        if (Main.KEY_DISABLE.consumeClick()) {
            Client client = Main.CLIENT_VOICE_EVENTS.getClient();
            if (client == null || !client.isAuthenticated()) {
                sendUnavailableMessage();
            } else {
                playerStateManager.setDisabled(!playerStateManager.isDisabled());
            }
        }

        if (Main.KEY_HIDE_ICONS.consumeClick()) {
            boolean hidden = !Main.CLIENT_CONFIG.hideIcons.get();
            Main.CLIENT_CONFIG.hideIcons.set(hidden);
            Main.CLIENT_CONFIG.hideIcons.save();

            if (hidden) {
                minecraft.player.displayClientMessage(new TranslationTextComponent("message.voicechat.icons_hidden"), true);
            } else {
                minecraft.player.displayClientMessage(new TranslationTextComponent("message.voicechat.icons_visible"), true);
            }
        }
    }

    public void sendUnavailableMessage() {
        minecraft.player.displayClientMessage(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), true);
    }

    @SubscribeEvent
    public void onRenderName(RenderNameplateEvent event) {
        if (Main.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!(event.getEntity() instanceof PlayerEntity)) {
            return;
        }
        if (event.getEntity() == minecraft.player) {
            return;
        }

        PlayerEntity player = (PlayerEntity) event.getEntity();

        if (!minecraft.options.hideGui) {
            if (playerStateManager.isPlayerDisconnected(player)) {
                renderPlayerIcon(player, DISCONNECT_ICON, event.getMatrixStack(), event.getRenderTypeBuffer(), event.getPackedLight());
            } else if (playerStateManager.isPlayerDisabled(player)) {
                renderPlayerIcon(player, SPEAKER_OFF_ICON, event.getMatrixStack(), event.getRenderTypeBuffer(), event.getPackedLight());
            } else if (client != null && client.getTalkCache().isTalking(player)) {
                renderPlayerIcon(player, SPEAKER_ICON, event.getMatrixStack(), event.getRenderTypeBuffer(), event.getPackedLight());
            }
        }
    }

    protected void renderPlayerIcon(PlayerEntity player, ResourceLocation texture, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0D, player.getBbHeight() + 0.5D, 0D);
        matrixStackIn.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(player.getDisplayName()) / 2 + 2);

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (player.isDiscrete()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, packedLightIn);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, packedLightIn);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, packedLightIn);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, packedLightIn);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, packedLightIn);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, packedLightIn);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, packedLightIn);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, packedLightIn);

            IVertexBuilder builderSeeThrough = bufferIn.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, packedLightIn);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, packedLightIn);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, packedLightIn);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, packedLightIn);
        }

        matrixStackIn.popPose();
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        MatrixStack.Entry entry = matrixStack.last();
        builder.vertex(entry.pose(), x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(entry.normal(), 0F, 0F, -1F)
                .endVertex();
    }

}
