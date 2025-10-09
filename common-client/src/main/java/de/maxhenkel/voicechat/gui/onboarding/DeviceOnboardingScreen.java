package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    public DeviceOnboardingScreen(Component title, @Nullable Screen previous) {
        super(title, previous);
    }

    public abstract AudioDeviceList createAudioDeviceList(int width, int height, int top);

    @Override
    protected void init() {
        super.init();

        if (deviceList != null) {
            deviceList.updateSize(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, 0, guiTop + font.lineHeight + PADDING);
        } else {
            deviceList = createAudioDeviceList(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, guiTop + font.lineHeight + PADDING);
        }
        addWidget(deviceList);

        addBackOrCancelButton();
        addNextButton();
    }

    @Override
    public abstract Screen getNextScreen();

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        deviceList.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTitle(guiGraphics, title);
    }

}
