package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.plugins.impl.VoicechatClientApiImpl;
import de.maxhenkel.voicechat.service.Service;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.repository.RepositorySource;

import java.net.SocketAddress;
import java.util.function.Consumer;

public abstract class ClientCompatibilityManager {

    public static ClientCompatibilityManager INSTANCE = Service.get(ClientCompatibilityManager.class);

    public abstract void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate);

    public abstract void onRenderHUD(RenderHUDEvent onRenderHUD);

    public abstract void onKeyboardEvent(KeyboardEvent onKeyboardEvent);

    public abstract void onMouseEvent(MouseEvent onMouseEvent);

    public abstract void onClientTick(Runnable onClientTick);

    public abstract InputConstants.Key getBoundKeyOf(KeyMapping keyBinding);

    public abstract void onHandleKeyBinds(Runnable onHandleKeyBinds);

    public abstract KeyMapping registerKeyBinding(KeyMapping keyBinding);

    public abstract void emitVoiceChatConnectedEvent(ClientVoicechatConnection client);

    public abstract void emitVoiceChatDisconnectedEvent();

    public abstract void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected);

    public abstract void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected);

    public abstract void emitDisconnectedEvent();

    public abstract void onDisconnect(Runnable onDisconnect);

    public abstract void onJoinWorld(Runnable onJoinWorld);

    public abstract void onPublishServer(Consumer<Integer> onPublishServer);

    public abstract SocketAddress getSocketAddress(Connection connection);

    public abstract void addResourcePackSource(RepositorySource repositorySource);

    public VoicechatClientApi getClientApi() {
        return VoicechatClientApiImpl.INSTANCE;
    }

    public interface RenderNameplateEvent {
        void render(EntityRenderState renderState, CameraRenderState cameraRenderState, PoseStack stack, SubmitNodeCollector collector);
    }

    public interface RenderHUDEvent {
        void render(GuiGraphics guiGraphics, float tickDelta);
    }

    public interface KeyboardEvent {
        void onKeyboardEvent(KeyEvent keyEvent);
    }

    public interface MouseEvent {
        void onMouseEvent(MouseButtonInfo mouseButtonInfo, int action);
    }

}
