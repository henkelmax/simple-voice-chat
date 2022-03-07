package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.voice.client.ALSpeaker;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.SpeakerException;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.function.Consumer;

public abstract class ClientCompatibilityManager {

    public static ClientCompatibilityManager INSTANCE;

    public abstract void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate);

    public abstract void onRenderHUD(RenderHUDEvent onRenderHUD);

    public abstract void onKeyboardEvent(KeyboardEvent onKeyboardEvent);

    public abstract void onMouseEvent(MouseEvent onMouseEvent);

    public abstract InputConstants.Key getBoundKeyOf(KeyMapping keyBinding);

    public abstract void onHandleKeyBinds(Runnable onHandleKeyBinds);

    public abstract KeyMapping registerKeyBinding(KeyMapping keyBinding);

    public abstract void emitVoiceChatConnectedEvent(ClientVoicechatConnection client);

    public abstract void emitVoiceChatDisconnectedEvent();

    public abstract void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected);

    public abstract void onVoiceChatDisconnected(Runnable onVoiceChatConnected);

    public abstract void onDisconnect(Runnable onDisconnect);

    public abstract void onJoinServer(Runnable onJoinServer);

    public abstract void onJoinWorld(Runnable onJoinWorld);

    public abstract SocketAddress getSocketAddress(Connection connection);

    public abstract void addResourcePackSource(PackRepository packRepository, RepositorySource repositorySource);

    public interface RenderNameplateEvent {
        void render(Entity entity, Component component, PoseStack stack, MultiBufferSource bufferSource, int light);
    }

    public interface RenderHUDEvent {
        void render(PoseStack stack, float tickDelta);
    }

    public interface KeyboardEvent {
        void onKeyboardEvent(long window, int key, int scancode);
    }

    public interface MouseEvent {
        void onMouseEvent(long window, int button, int action, int mods);
    }

}
