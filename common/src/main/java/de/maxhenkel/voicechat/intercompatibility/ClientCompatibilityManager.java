package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.voice.client.ALSpeaker;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.SpeakerException;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.function.Consumer;

public abstract class ClientCompatibilityManager {

    public static ClientCompatibilityManager INSTANCE;

    public abstract void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate);

    public abstract void onRenderHUD(RenderHUDEvent onRenderHUD);

    public abstract void onKeyboardEvent(KeyboardEvent onKeyboardEvent);

    public abstract void onMouseEvent(MouseEvent onMouseEvent);

    public abstract InputMappings.Input getBoundKeyOf(KeyBinding keyBinding);

    public abstract void onHandleKeyBinds(Runnable onHandleKeyBinds);

    public abstract KeyBinding registerKeyBinding(KeyBinding keyBinding);

    public abstract void emitVoiceChatConnectedEvent(ClientVoicechatConnection client);

    public abstract void emitVoiceChatDisconnectedEvent();

    public abstract void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected);

    public abstract void onVoiceChatDisconnected(Runnable onVoiceChatConnected);

    public abstract void onDisconnect(Runnable onDisconnect);

    public abstract void onJoinServer(Runnable onJoinServer);

    public abstract void onJoinWorld(Runnable onJoinWorld);

    public abstract SocketAddress getSocketAddress(NetworkManager connection);

    public abstract void addResourcePackSource(ResourcePackList packRepository, IPackFinder repositorySource);

    public abstract ALSpeaker createSpeaker(SoundManager soundManager, int sampleRate, int bufferSize);

    public abstract SoundManager createSoundManager(@Nullable String deviceName) throws SpeakerException;

    public interface RenderNameplateEvent {
        void render(Entity entity, ITextComponent component, MatrixStack stack, IRenderTypeBuffer bufferSource, int light);
    }

    public interface RenderHUDEvent {
        void render(MatrixStack stack, float tickDelta);
    }

    public interface KeyboardEvent {
        void onKeyboardEvent(long window, int key, int scancode);
    }

    public interface MouseEvent {
        void onMouseEvent(long window, int button, int action, int mods);
    }

}
