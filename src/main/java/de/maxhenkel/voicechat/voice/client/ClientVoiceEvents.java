package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class ClientVoiceEvents {

    private static final ResourceLocation VOICE_ICON = new ResourceLocation(Main.MODID, "textures/gui/microphone.png");

    private Client client;
    private Minecraft minecraft;

    public ClientVoiceEvents() {
        minecraft = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void joinEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() != minecraft.player) {
            return;
        }
        if (client != null) {
            return;
        }
        ServerData serverData = minecraft.getCurrentServerData();
        if (serverData != null) {
            try {
                Main.LOGGER.info("Connecting to server: '" + serverData.serverIP + ":" + Config.SERVER.VOICE_CHAT_PORT.get() + "'");
                client = new Client(serverData.serverIP, Config.SERVER.VOICE_CHAT_PORT.get());
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @SubscribeEvent
    public void joinEvent(WorldEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.playerController == null) {
            if (client != null) {
                client.close();
                client = null;
            }
        }
    }

    public Client getClient() {
        return client;
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR)) {
            return;
        }

        if (client == null || !client.isConnected()) {
            return;
        }

        if (!Main.KEY_PTT.isKeyDown()) {
            return;
        }

        RenderSystem.pushMatrix();

        minecraft.getTextureManager().bindTexture(VOICE_ICON);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        //double width = minecraft.getMainWindow().getScaledWidth();
        double height = minecraft.getMainWindow().getScaledHeight();

        buffer.pos(16D, height - 32D, 0D).tex(0F, 0F).endVertex();
        buffer.pos(16D, height - 16D, 0D).tex(0F, 1F).endVertex();
        buffer.pos(32D, height - 16D, 0D).tex(1F, 1F).endVertex();
        buffer.pos(32D, height - 32D, 0D).tex(1F, 0F).endVertex();

        tessellator.draw();

        RenderSystem.popMatrix();
    }

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        if (Main.KEY_VOICE_CHAT_SETTINGS.isPressed()) {
            minecraft.displayGuiScreen(new VoiceChatScreen());
        }
    }

}
