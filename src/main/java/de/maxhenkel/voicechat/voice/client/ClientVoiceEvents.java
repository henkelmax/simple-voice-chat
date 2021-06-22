package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.ClientVoiceChatEvents;
import de.maxhenkel.voicechat.events.ClientWorldEvents;
import de.maxhenkel.voicechat.events.IClientConnection;
import de.maxhenkel.voicechat.events.RenderEvents;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.GroupScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.net.InitPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SetGroupPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientVoiceEvents {

    private static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/microphone.png");
    private static final ResourceLocation MICROPHONE_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/microphone_off.png");
    private static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_off.png");
    private static final ResourceLocation DISCONNECT_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/disconnected.png");
    private static final ResourceLocation GROUP_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/group.png");

    private Client client;
    private ClientPlayerStateManager playerStateManager;
    private PTTKeyHandler pttKeyHandler;
    private Minecraft minecraft;

    public ClientVoiceEvents() {
        playerStateManager = new ClientPlayerStateManager();
        pttKeyHandler = new PTTKeyHandler();
        minecraft = Minecraft.getInstance();

        ClientWorldEvents.DISCONNECT.register(this::onDisconnect);

        HudRenderCallback.EVENT.register(this::renderHUD);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
        RenderEvents.RENDER_NAMEPLATE.register(this::onRenderName);

        NetManager.registerClientReceiver(InitPacket.class, (client, handler, responseSender, packet) -> {
            authenticate(handler.getLocalGameProfile().getId(), packet);
        });

        NetManager.registerClientReceiver(SetGroupPacket.class, (client, handler, responseSender, packet) -> {
            playerStateManager.setGroup(packet.getGroup());
            minecraft.setScreen(null);
        });
    }

    public void authenticate(UUID playerUUID, InitPacket initPacket) {
        Voicechat.LOGGER.info("Received secret");
        if (client != null) {
            onDisconnect();
        }
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            try {
                String ip;
                SocketAddress socketAddress = ((IClientConnection) connection.getConnection()).getChannel().remoteAddress();
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    ip = address.getHostString();
                    String initIP = initPacket.getVoiceHost();
                    if (!initIP.isEmpty()) {
                        ip = initIP;
                    }
                    Voicechat.LOGGER.info("Connecting to server: '" + ip + ":" + initPacket.getServerPort() + "'");
                    client = new Client(ip, initPacket.getServerPort(), playerUUID, initPacket.getSecret(), initPacket.getCodec(), initPacket.getMtuSize(), initPacket.getVoiceChatDistance(), initPacket.getVoiceChatFadeDistance(), initPacket.getKeepAlive(), initPacket.groupsEnabled());
                    client.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDisconnect() {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.invoker().run();
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

    public PTTKeyHandler getPttKeyHandler() {
        return pttKeyHandler;
    }

    public void renderHUD(PoseStack stack, float tickDelta) {
        if (!isMultiplayerServer()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }

        if (playerStateManager.isDisconnected()) {
            renderIcon(stack, DISCONNECT_ICON);
        } else if (playerStateManager.isDisabled()) {
            renderIcon(stack, SPEAKER_OFF_ICON);
        } else if (playerStateManager.isMuted() && VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(stack, MICROPHONE_OFF_ICON);
        } else if (client != null && client.getMicThread() != null && client.getMicThread().isTalking()) {
            renderIcon(stack, MICROPHONE_ICON);
        }

        if (playerStateManager.isInGroup() && VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            GroupChatManager.renderIcons(stack);
        }
    }

    private void renderIcon(PoseStack matrixStack, ResourceLocation texture) {
        matrixStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        HUDIconLocation location = VoicechatClient.CLIENT_CONFIG.hudIconLocation.get();
        if (location.equals(HUDIconLocation.RIGHT)) {
            Screen.blit(matrixStack, width - 32, height - 32, 0, 0, 16, 16, 16, 16);
        } else if (location.equals(HUDIconLocation.CENTER)) {
            if (minecraft.gameMode != null && minecraft.gameMode.hasExperience()) {
                Screen.blit(matrixStack, width / 2 - 8, height - 16 - 35 - 2, 0, 0, 16, 16, 16, 16);
            } else {
                Screen.blit(matrixStack, width / 2 - 8, height - 35 - 4, 0, 0, 16, 16, 16, 16);
            }
        } else {
            Screen.blit(matrixStack, 16, height - 32, 0, 0, 16, 16, 16, 16);
        }
        matrixStack.popPose();
    }

    public void onClientTickEnd(Minecraft minecraft) {
        if (VoicechatClient.KEY_VOICE_CHAT.consumeClick() && checkConnected()) {
            minecraft.setScreen(new VoiceChatScreen());
        }

        if (VoicechatClient.KEY_GROUP.consumeClick() && checkConnected()) {
            if (client.groupsEnabled()) {
                if (playerStateManager.isInGroup()) {
                    minecraft.setScreen(new GroupScreen());
                } else {
                    minecraft.setScreen(new CreateGroupScreen());
                }
            } else {
                minecraft.player.displayClientMessage(new TranslatableComponent("message.voicechat.groups_disabled"), true);
            }
        }

        if (VoicechatClient.KEY_VOICE_CHAT_SETTINGS.consumeClick() && checkConnected()) {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        }

        if (VoicechatClient.KEY_PTT.consumeClick()) {
            checkConnected();
        }

        if (VoicechatClient.KEY_MUTE.consumeClick() && checkConnected()) {
            playerStateManager.setMuted(!playerStateManager.isMuted());
        }

        if (VoicechatClient.KEY_DISABLE.consumeClick() && checkConnected()) {
            playerStateManager.setDisabled(!playerStateManager.isDisabled());
        }

        if (VoicechatClient.KEY_HIDE_ICONS.consumeClick()) {
            boolean hidden = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(hidden);
            VoicechatClient.CLIENT_CONFIG.hideIcons.save();

            if (hidden) {
                minecraft.player.displayClientMessage(new TranslatableComponent("message.voicechat.icons_hidden"), true);
            } else {
                minecraft.player.displayClientMessage(new TranslatableComponent("message.voicechat.icons_visible"), true);
            }
        }
    }

    public boolean checkConnected() {
        if (VoicechatClient.CLIENT.getClient() == null || !VoicechatClient.CLIENT.getClient().isAuthenticated()) {
            sendUnavailableMessage();
            return false;
        }
        return true;
    }

    public void sendUnavailableMessage() {
        minecraft.player.displayClientMessage(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), true);
    }

    public boolean isMultiplayerServer() {
        return minecraft.getCurrentServer() != null && !minecraft.getCurrentServer().isLan();
    }

    public void onRenderName(Entity entity, Component component, PoseStack stack, MultiBufferSource vertexConsumers, int light) {
        if (!isMultiplayerServer()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        if (entity == minecraft.player) {
            return;
        }

        Player player = (Player) entity;

        if (!minecraft.options.hideGui) {
            String group = playerStateManager.getGroup(player);

            if (client != null && client.getTalkCache().isTalking(player)) {
                renderPlayerIcon(player, component, SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (playerStateManager.isPlayerDisconnected(player)) {
                renderPlayerIcon(player, component, DISCONNECT_ICON, stack, vertexConsumers, light);
            } else if (group != null && !group.equals(playerStateManager.getGroup())) {
                renderPlayerIcon(player, component, GROUP_ICON, stack, vertexConsumers, light);
            } else if (playerStateManager.isPlayerDisabled(player)) {
                renderPlayerIcon(player, component, SPEAKER_OFF_ICON, stack, vertexConsumers, light);
            }
        }
    }

    protected void renderPlayerIcon(Player player, Component component, ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource buffer, int light) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0D, player.getBbHeight() + 0.5D, 0D);
        matrixStackIn.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(component) / 2 + 2);

        VertexConsumer builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (player.isDiscrete()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        }

        matrixStackIn.popPose();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrixStack.last();
        builder.vertex(entry.pose(), x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(entry.normal(), 0F, 0F, -1F)
                .endVertex();
    }

}
