package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.*;
import de.maxhenkel.voicechat.mixin.ConnectionAccessor;
import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

import java.net.SocketAddress;
import java.util.function.Consumer;

public class QuiltClientCompatibilityManager extends ClientCompatibilityManager {

    private static final ResourceLocation EARLY_JOIN = new ResourceLocation(Voicechat.MODID, "early_join");

    private static final Minecraft mc = Minecraft.getInstance();

    public QuiltClientCompatibilityManager() {
        ClientPlayConnectionEvents.JOIN.addPhaseOrdering(EARLY_JOIN, Event.DEFAULT_PHASE);
    }

    @Override
    public void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate) {
        RenderEvents.RENDER_NAMEPLATE.register(onRenderNamePlate);
    }

    @Override
    public void onRenderHUD(RenderHUDEvent onRenderHUD) {
        RenderEvents.RENDER_HUD.register(guiGraphics -> onRenderHUD.render(guiGraphics, mc.getFrameTime()));
    }

    @Override
    public void onKeyboardEvent(KeyboardEvent onKeyboardEvent) {
        InputEvents.KEYBOARD_KEY.register(onKeyboardEvent);
    }

    @Override
    public void onMouseEvent(MouseEvent onMouseEvent) {
        InputEvents.MOUSE_KEY.register(onMouseEvent);
    }

    @Override
    public void onClientTick(Runnable onClientTick) {
        ClientTickEvents.START.register(client -> onClientTick.run());
    }

    @Override
    public void onRenderTick(Runnable onRenderTick) {
        RenderEvents.RENDER_TICK.register(onRenderTick);
    }

    @Override
    public InputConstants.Key getBoundKeyOf(KeyMapping keyBinding) {
        return KeyBindingHelper.getBoundKeyOf(keyBinding);
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        InputEvents.HANDLE_KEYBINDS.register(onHandleKeyBinds);
    }

    @Override
    public KeyMapping registerKeyBinding(KeyMapping keyBinding) {
        return KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.invoker().accept(client);
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.invoker().run();
    }

    @Override
    public void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.register(onVoiceChatConnected);
    }

    @Override
    public void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected) {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.register(onVoiceChatDisconnected);
    }

    @Override
    public void emitDisconnectedEvent() {
        ClientWorldEvents.DISCONNECT.invoker().run();
    }

    @Override
    public void onDisconnect(Runnable onDisconnect) {
        ClientWorldEvents.DISCONNECT.register(onDisconnect);
    }

    @Override
    public void onJoinWorld(Runnable onJoinWorld) {
        // Higher priority to prevent this from not firing in case another mod throws an exception in this event
        ClientPlayConnectionEvents.JOIN.register(EARLY_JOIN, (handler, sender, client) -> onJoinWorld.run());
    }

    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        PublishServerEvents.SERVER_PUBLISHED.register(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(Connection connection) {
        return ((ConnectionAccessor) connection).getChannel().remoteAddress();
    }

    @Override
    public void addResourcePackSource(PackRepository packRepository, RepositorySource repositorySource) {
        IPackRepository repository = (IPackRepository) packRepository;
        repository.voicechat$addSource(repositorySource);
    }
}
