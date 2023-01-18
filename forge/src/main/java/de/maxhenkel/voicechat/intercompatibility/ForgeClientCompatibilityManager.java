package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.ClientVoiceChatConnectedEvent;
import de.maxhenkel.voicechat.events.ClientVoiceChatDisconnectedEvent;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ForgeClientCompatibilityManager extends ClientCompatibilityManager {

    private final Minecraft minecraft;

    private final List<RenderNameplateEvent> renderNameplateEvents;
    private final List<RenderHUDEvent> renderHUDEvents;
    private final List<KeyboardEvent> keyboardEvents;
    private final List<MouseEvent> mouseEvents;
    private final List<Runnable> inputEvents;
    private final List<Runnable> disconnectEvents;
    private final List<Runnable> joinServerEvents;
    private final List<Runnable> joinWorldEvents;
    private final List<Consumer<ClientVoicechatConnection>> voicechatConnectEvents;
    private final List<Runnable> voicechatDisconnectEvents;
    private final List<Consumer<Integer>> publishServerEvents;

    public ForgeClientCompatibilityManager() {
        minecraft = Minecraft.getMinecraft();
        renderNameplateEvents = new ArrayList<>();
        renderHUDEvents = new ArrayList<>();
        keyboardEvents = new ArrayList<>();
        mouseEvents = new ArrayList<>();
        inputEvents = new ArrayList<>();
        disconnectEvents = new ArrayList<>();
        joinServerEvents = new ArrayList<>();
        joinWorldEvents = new ArrayList<>();
        voicechatConnectEvents = new ArrayList<>();
        voicechatDisconnectEvents = new ArrayList<>();
        publishServerEvents = new ArrayList<>();
    }

    public void onRenderName(Entity entity, String str, double x, double y, double z, int maxDistance) {
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(entity, str, x, y, z, maxDistance));
        //TODO Check if player can be seen
        if (minecraft.player == null /*|| entity.isInvisibleTo(minecraft.player)*/) {
            return;
        }
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(entity, str, x, y, z, maxDistance));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.ALL)) {
            return;
        }
        renderHUDEvents.forEach(renderHUDEvent -> renderHUDEvent.render(event.getPartialTicks()));
    }

    public void onTickKey() {
        keyboardEvents.forEach(KeyboardEvent::onKeyboardEvent);
    }

    public void onTickMouse() {
        mouseEvents.forEach(MouseEvent::onMouseEvent);
    }

    @SubscribeEvent
    public void onInput(TickEvent.ClientTickEvent event) {
        inputEvents.forEach(Runnable::run);
    }

    public void onDisconnect() {
        disconnectEvents.forEach(Runnable::run);
    }

    @SubscribeEvent
    public void onJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player != minecraft.player) {
            return;
        }
        joinServerEvents.forEach(Runnable::run);
        joinWorldEvents.forEach(Runnable::run);
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() != minecraft.player) {
            return;
        }
        joinWorldEvents.forEach(Runnable::run);
    }

    public void onOpenPort(int port) {
        publishServerEvents.forEach(consumer -> consumer.accept(port));
    }

    @Override
    public void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate) {
        renderNameplateEvents.add(onRenderNamePlate);
    }

    @Override
    public void onRenderHUD(RenderHUDEvent onRenderHUD) {
        renderHUDEvents.add(onRenderHUD);
    }

    @Override
    public void onKeyboardEvent(KeyboardEvent onKeyboardEvent) {
        keyboardEvents.add(onKeyboardEvent);
    }

    @Override
    public void onMouseEvent(MouseEvent onMouseEvent) {
        mouseEvents.add(onMouseEvent);
    }

    @Override
    public int getBoundKeyOf(KeyBinding keyBinding) {
        return keyBinding.getKeyCode();
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        inputEvents.add(onHandleKeyBinds);
    }

    @Override
    public KeyBinding registerKeyBinding(KeyBinding keyBinding) {
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(client));
        MinecraftForge.EVENT_BUS.post(new ClientVoiceChatConnectedEvent(client));
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        voicechatDisconnectEvents.forEach(Runnable::run);
        MinecraftForge.EVENT_BUS.post(new ClientVoiceChatDisconnectedEvent());
    }

    @Override
    public void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected) {
        voicechatConnectEvents.add(onVoiceChatConnected);
    }

    @Override
    public void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected) {
        voicechatDisconnectEvents.add(onVoiceChatDisconnected);
    }

    @Override
    public void onDisconnect(Runnable onDisconnect) {
        disconnectEvents.add(onDisconnect);
    }

    @Override
    public void onJoinServer(Runnable onJoinServer) {
        joinServerEvents.add(onJoinServer);
    }

    @Override
    public void onJoinWorld(Runnable onJoinWorld) {
        joinWorldEvents.add(onJoinWorld);
    }

    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        publishServerEvents.add(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(NetworkManager connection) {
        return connection.channel().remoteAddress();
    }

}
