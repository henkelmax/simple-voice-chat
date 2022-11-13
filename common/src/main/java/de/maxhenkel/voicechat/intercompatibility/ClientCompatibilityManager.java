package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.service.Service;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;

import java.net.SocketAddress;
import java.util.function.Consumer;

public abstract class ClientCompatibilityManager {

    public static ClientCompatibilityManager INSTANCE = Service.get(ClientCompatibilityManager.class);

    public abstract void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate);

    public abstract void onRenderHUD(RenderHUDEvent onRenderHUD);

    public abstract void onKeyboardEvent(KeyboardEvent onKeyboardEvent);

    public abstract void onMouseEvent(MouseEvent onMouseEvent);

    public abstract int getBoundKeyOf(KeyBinding keyBinding);

    public abstract void onHandleKeyBinds(Runnable onHandleKeyBinds);

    public abstract KeyBinding registerKeyBinding(KeyBinding keyBinding);

    public abstract void emitVoiceChatConnectedEvent(ClientVoicechatConnection client);

    public abstract void emitVoiceChatDisconnectedEvent();

    public abstract void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected);

    public abstract void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected);

    public abstract void onDisconnect(Runnable onDisconnect);

    public abstract void onJoinServer(Runnable onJoinServer);

    public abstract void onJoinWorld(Runnable onJoinWorld);

    public abstract void onPublishServer(Consumer<Integer> onPublishServer);

    public abstract SocketAddress getSocketAddress(NetworkManager connection);

    public interface RenderNameplateEvent {
        void render(Entity entity, String str, double x, double y, double z, int maxDistance);
    }

    public interface RenderHUDEvent {
        void render(float tickDelta);
    }

    public interface KeyboardEvent {
        void onKeyboardEvent();
    }

    public interface MouseEvent {
        void onMouseEvent();
    }

}
