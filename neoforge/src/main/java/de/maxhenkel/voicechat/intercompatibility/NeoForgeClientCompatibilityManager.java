package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.events.ClientVoiceChatConnectedEvent;
import de.maxhenkel.voicechat.events.ClientVoiceChatDisconnectedEvent;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NeoForgeClientCompatibilityManager extends ClientCompatibilityManager {

    private final Minecraft minecraft;

    private final List<RenderNameplateEvent> renderNameplateEvents;
    private final List<RenderHUDEvent> renderHUDEvents;
    private final List<KeyboardEvent> keyboardEvents;
    private final List<MouseEvent> mouseEvents;
    private final List<Runnable> clientTickEvents;
    private final List<Runnable> inputEvents;
    private final List<Runnable> disconnectEvents;
    private final List<Runnable> joinWorldEvents;
    private final List<Consumer<ClientVoicechatConnection>> voicechatConnectEvents;
    private final List<Runnable> voicechatDisconnectEvents;
    private final List<Consumer<Integer>> publishServerEvents;
    private final List<KeyMapping> keyMappings;

    public NeoForgeClientCompatibilityManager() {
        minecraft = Minecraft.getInstance();
        renderNameplateEvents = new ArrayList<>();
        renderHUDEvents = new ArrayList<>();
        keyboardEvents = new ArrayList<>();
        mouseEvents = new ArrayList<>();
        clientTickEvents = new ArrayList<>();
        inputEvents = new ArrayList<>();
        disconnectEvents = new ArrayList<>();
        joinWorldEvents = new ArrayList<>();
        voicechatConnectEvents = new ArrayList<>();
        voicechatDisconnectEvents = new ArrayList<>();
        publishServerEvents = new ArrayList<>();
        keyMappings = new ArrayList<>();
    }

    @SubscribeEvent
    public void onRenderName(net.neoforged.neoforge.client.event.RenderNameTagEvent event) {
        if (minecraft.player == null || event.getEntity().isInvisibleTo(minecraft.player)) {
            return;
        }
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        renderHUDEvents.forEach(renderHUDEvent -> renderHUDEvent.render(event.getGuiGraphics(), event.getPartialTick()));
    }

    @SubscribeEvent
    public void onKey(InputEvent.Key event) {
        keyboardEvents.forEach(keyboardEvent -> keyboardEvent.onKeyboardEvent(minecraft.getWindow().getWindow(), event.getKey(), event.getScanCode()));
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseButton.Pre event) {
        mouseEvents.forEach(mouseEvent -> mouseEvent.onMouseEvent(minecraft.getWindow().getWindow(), event.getButton(), event.getAction(), event.getModifiers()));
    }

    @SubscribeEvent
    public void onKeyInput(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        clientTickEvents.forEach(Runnable::run);
    }

    @SubscribeEvent
    public void onInput(TickEvent.ClientTickEvent event) {
        inputEvents.forEach(Runnable::run);
    }

    @SubscribeEvent
    public void onDisconnect(LevelEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.gameMode == null) {
            disconnectEvents.forEach(Runnable::run);
        }
    }

    @SubscribeEvent
    public void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        if (event.getPlayer() != minecraft.player) {
            return;
        }
        joinWorldEvents.forEach(Runnable::run);
    }

    private boolean wasPublished;

    @SubscribeEvent
    public void onServer(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) {
            return;
        }
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }

        boolean published = server.isPublished();

        if (published && !wasPublished) {
            publishServerEvents.forEach(portConsumer -> portConsumer.accept(server.getPort()));
        }

        wasPublished = published;
    }

    public void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        for (KeyMapping mapping : keyMappings) {
            event.register(mapping);
        }
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
    public void onClientTick(Runnable onClientTick) {
        clientTickEvents.add(onClientTick);
    }

    @Override
    public InputConstants.Key getBoundKeyOf(KeyMapping keyBinding) {
        return keyBinding.getKey();
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        inputEvents.add(onHandleKeyBinds);
    }

    @Override
    public KeyMapping registerKeyBinding(KeyMapping keyBinding) {
        keyMappings.add(keyBinding);
        return keyBinding;
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(client));
        NeoForge.EVENT_BUS.post(new ClientVoiceChatConnectedEvent(client));
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        voicechatDisconnectEvents.forEach(Runnable::run);
        NeoForge.EVENT_BUS.post(new ClientVoiceChatDisconnectedEvent());
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
    public void onJoinWorld(Runnable onJoinWorld) {
        joinWorldEvents.add(onJoinWorld);
    }

    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        publishServerEvents.add(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(Connection connection) {
        return connection.channel().remoteAddress();
    }

    @Override
    public void addResourcePackSource(PackRepository packRepository, RepositorySource repositorySource) {
        packRepository.addPackFinder(repositorySource);
    }
}
