package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ForgeClientCompatibilityManager extends ClientCompatibilityManager {

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

    public ForgeClientCompatibilityManager() {
        minecraft = Minecraft.getInstance();
        renderNameplateEvents = new CopyOnWriteArrayList<>();
        renderHUDEvents = new CopyOnWriteArrayList<>();
        keyboardEvents = new CopyOnWriteArrayList<>();
        mouseEvents = new CopyOnWriteArrayList<>();
        clientTickEvents = new CopyOnWriteArrayList<>();
        inputEvents = new CopyOnWriteArrayList<>();
        disconnectEvents = new CopyOnWriteArrayList<>();
        joinWorldEvents = new CopyOnWriteArrayList<>();
        voicechatConnectEvents = new CopyOnWriteArrayList<>();
        voicechatDisconnectEvents = new CopyOnWriteArrayList<>();
        publishServerEvents = new CopyOnWriteArrayList<>();
        keyMappings = new CopyOnWriteArrayList<>();
    }

    public void onRenderName(net.minecraftforge.client.event.RenderNameTagEvent event) {
        renderNameplateEvents.forEach(renderNameplateEvent -> renderNameplateEvent.render(event.getState(), event.getCameraState(), event.getPoseStack(), event.getNodeCollector()));
    }

    private static final Identifier VOICECHAT_ICONS_LAYER = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons");

    public void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(VOICECHAT_ICONS_LAYER, (gg, dt) -> {
            renderHUDEvents.forEach(renderHUDEvent -> renderHUDEvent.render(gg, dt.getRealtimeDeltaTicks()));
        });
        event.getLayeredDraw().putBelow(ForgeLayeredDraw.VANILLA_ROOT, VOICECHAT_ICONS_LAYER, ForgeLayeredDraw.POTION_EFFECTS);
    }

    public void onKey(InputEvent.Key event) {
        keyboardEvents.forEach(keyboardEvent -> keyboardEvent.onKeyboardEvent(new KeyEvent(event.getKey(), event.getScanCode(), event.getModifiers())));
    }

    public void onMouse(InputEvent.MouseButton.Pre event) {
        mouseEvents.forEach(mouseEvent -> mouseEvent.onMouseEvent(new MouseButtonInfo(event.getButton(), event.getModifiers()), event.getAction()));
    }

    public void onClientTick(TickEvent.ClientTickEvent.Pre event) {
        clientTickEvents.forEach(Runnable::run);
    }

    public void onKeyInput(TickEvent.ClientTickEvent.Post event) {
        inputEvents.forEach(Runnable::run);
    }

    public void onDisconnect(LevelEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.gameMode == null) {
            disconnectEvents.forEach(Runnable::run);
        }
    }

    public void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        if (event.getPlayer() != minecraft.player) {
            return;
        }
        joinWorldEvents.forEach(Runnable::run);
    }

    private boolean wasPublished;

    public void onServer(TickEvent.ServerTickEvent event) {
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
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        voicechatDisconnectEvents.forEach(Runnable::run);
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
    public void emitDisconnectedEvent() {
        disconnectEvents.forEach(Runnable::run);
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
    public void addResourcePackSource(RepositorySource repositorySource) {
        minecraft.getResourcePackRepository().addPackFinder(repositorySource);
    }
}
