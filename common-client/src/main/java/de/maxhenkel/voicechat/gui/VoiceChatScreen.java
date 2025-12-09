package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.tooltips.DisableTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.HideTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.MuteTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.RecordingTooltipSupplier;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final ResourceLocation HIDE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/hide_button.png");
    private static final ResourceLocation VOLUMES = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/adjust_volumes.png");
    private static final ResourceLocation SPEAKER = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_button.png");
    private static final ResourceLocation RECORD = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/record_button.png");
    private static final ResourceLocation RECONNECT_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/reconnect_button.png");
    private static final ResourceLocation[] RECONNECT_SPINNER = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/reconnect_spinner_0.png"),
            ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/reconnect_spinner_1.png"),
            ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/reconnect_spinner_2.png"),
            ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/reconnect_spinner_3.png")
    };
    private static final Component TITLE = Component.translatable("gui.voicechat.voice_chat.title");
    private static final Component SETTINGS = Component.translatable("message.voicechat.settings");
    private static final Component GROUP = Component.translatable("message.voicechat.group");
    private static final Component RECONNECT = Component.translatable("message.voicechat.reconnect");
    private static final Component DEBUG_DISCONNECT = Component.translatable("message.voicechat.debug_disconnect");
    public static final Component ADJUST_PLAYER_VOLUMES = Component.translatable("message.voicechat.adjust_volumes");

    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private ImageButton reconnectButton;
    @Nullable
    private Button debugDisconnect;
    private HoverArea recordingHoverArea;
    private boolean reconnecting;
    private int reconnectAnimationTick;
    private int reconnectAnimationFrame;
    private static final int RECONNECT_ANIMATION_INTERVAL = 5;
    private static final int RECONNECT_ANIMATION_TIMEOUT = 200;

    private ClientPlayerStateManager stateManager;

    public VoiceChatScreen() {
        super(TITLE, 195, 76);
        stateManager = ClientManager.getPlayerStateManager();
    }

    @Override
    protected void init() {
        super.init();
        @Nullable ClientVoicechat client = ClientManager.getClient();

        int buttonSize = 20;
        int buttonSpacing = 2;
        int bottomButtonY = guiTop + ySize - 6 - buttonSize;
        int nextButtonX = guiLeft + 6;

        mute = new ToggleImageButton(nextButtonX, bottomButtonY, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addRenderableWidget(mute);
        nextButtonX += buttonSize + buttonSpacing;

        disable = new ToggleImageButton(nextButtonX, bottomButtonY, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addRenderableWidget(disable);
        nextButtonX += buttonSize + buttonSpacing;

        ImageButton volumes = new ImageButton(nextButtonX, bottomButtonY, VOLUMES, button -> {
            minecraft.setScreen(new AdjustVolumesScreen());
        });
        volumes.setTooltip(Tooltip.create(ADJUST_PLAYER_VOLUMES));
        addRenderableWidget(volumes);
        nextButtonX += buttonSize + buttonSpacing;

        reconnectButton = new ImageButton(nextButtonX, bottomButtonY, RECONNECT_ICON, button -> startReconnect());
        reconnectButton.setTooltip(Tooltip.create(RECONNECT));
        addRenderableWidget(reconnectButton);
        nextButtonX += buttonSize + buttonSpacing;

        boolean hasRecordButton = false;
        if (client != null && VoicechatClient.CLIENT_CONFIG.useNatives.get()) {
            if (client.getRecorder() != null || (client.getConnection() != null && client.getConnection().getData().allowRecording())) {
                hasRecordButton = true;
                ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - buttonSize - buttonSpacing - buttonSize, bottomButtonY, RECORD, () -> ClientManager.getClient() != null && ClientManager.getClient().getRecorder() != null, button -> toggleRecording(), new RecordingTooltipSupplier(this));
                addRenderableWidget(record);
            }
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - buttonSize, bottomButtonY, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get()).save();
        }, new HideTooltipSupplier(this));
        addRenderableWidget(hide);

        Button settings = Button.builder(SETTINGS, button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        }).bounds(guiLeft + 6, guiTop + 6 + 15, 75, 20).build();
        addRenderableWidget(settings);

        Button group = Button.builder(GROUP, button -> {
            ClientGroup g = stateManager.getGroup();
            if (g != null) {
                minecraft.setScreen(new GroupScreen(g));
            } else {
                minecraft.setScreen(new JoinGroupScreen());
            }
        }).bounds(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20).build();
        addRenderableWidget(group);

        if (Voicechat.debugMode()) {
            int debugY = guiTop + 6 + 15 + 22;
            debugDisconnect = Button.builder(DEBUG_DISCONNECT, button -> ClientManager.simulateVoiceChatDisconnect()).bounds(guiLeft + 6, debugY, xSize - 12, 20).build();
            addRenderableWidget(debugDisconnect);
        } else {
            debugDisconnect = null;
        }

        group.active = client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled();

        int leftButtons = 4;
        int leftOccupied = 6 + leftButtons * buttonSize + (leftButtons - 1) * buttonSpacing;
        int recordingAreaStart = leftOccupied + buttonSpacing;
        int rightButtons = 1 + (hasRecordButton ? 1 : 0);
        int rightOccupied = 6 + rightButtons * buttonSize + rightButtons * buttonSpacing;
        int recordingAreaWidth = Math.max(0, xSize - recordingAreaStart - rightOccupied);
        recordingHoverArea = new HoverArea(recordingAreaStart, ySize - 6 - buttonSize, recordingAreaWidth, buttonSize);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
        updateReconnectAnimation();
    }

    private void checkButtons() {
        mute.active = MuteTooltipSupplier.canMuteMic();
        disable.active = stateManager.canEnable();
        if (reconnectButton != null) {
            reconnectButton.active = !reconnecting && minecraft.player != null;
        }
        if (debugDisconnect != null) {
            debugDisconnect.active = minecraft.getConnection() != null && !stateManager.isDisconnected();
        }
    }

    private void startReconnect() {
        if (reconnecting) {
            return;
        }
        if (!ClientManager.reconnect()) {
            return;
        }
        reconnecting = true;
        reconnectAnimationTick = 0;
        reconnectAnimationFrame = 0;
        if (reconnectButton != null) {
            reconnectButton.setTexture(RECONNECT_SPINNER[reconnectAnimationFrame]);
            reconnectButton.active = false;
        }
    }

    private void updateReconnectAnimation() {
        if (!reconnecting || reconnectButton == null) {
            return;
        }
        reconnectAnimationTick++;
        if (reconnectAnimationTick % RECONNECT_ANIMATION_INTERVAL == 0) {
            reconnectAnimationFrame = (reconnectAnimationFrame + 1) % RECONNECT_SPINNER.length;
            reconnectButton.setTexture(RECONNECT_SPINNER[reconnectAnimationFrame]);
        }
        if (!stateManager.isDisconnected() || reconnectAnimationTick >= RECONNECT_ANIMATION_TIMEOUT) {
            stopReconnectAnimation();
        }
    }

    private void stopReconnectAnimation() {
        reconnecting = false;
        reconnectAnimationTick = 0;
        reconnectAnimationFrame = 0;
        if (reconnectButton != null) {
            reconnectButton.setTexture(RECONNECT_ICON);
            reconnectButton.active = minecraft.player != null;
        }
    }

    private void toggleRecording() {
        ClientVoicechat c = ClientManager.getClient();
        if (c == null) {
            return;
        }
        c.toggleRecording();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()) {
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        guiGraphics.drawString(font, TITLE, guiLeft + (xSize - titleWidth) / 2, guiTop + 7, FONT_COLOR, false);

        ClientVoicechat client = ClientManager.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            MutableComponent time = Component.literal(recorder.getDuration());
            guiGraphics.drawString(font, time.withStyle(ChatFormatting.DARK_RED), guiLeft + recordingHoverArea.getPosX() + recordingHoverArea.getWidth() / 2 - font.width(time) / 2, guiTop + recordingHoverArea.getPosY() + recordingHoverArea.getHeight() / 2 - font.lineHeight / 2, 0xFF000000, false);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(font, Component.translatable("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
