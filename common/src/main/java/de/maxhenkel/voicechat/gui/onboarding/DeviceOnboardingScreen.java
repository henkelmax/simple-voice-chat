package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    protected List<String> micNames;

    public DeviceOnboardingScreen(Component title, @Nullable Screen previous) {
        super(title, previous);
        micNames = getNames();
        if (micNames.isEmpty()) {
            minecraft.setScreen(getNextScreen());
        }
    }

    public abstract List<String> getNames();

    public abstract ResourceLocation getIcon();

    public abstract ConfigEntry<String> getConfigEntry();

    @Override
    protected void init() {
        super.init();

        if (deviceList != null) {
            deviceList.setRectangle(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, 0, guiTop + font.lineHeight + PADDING);
        } else {
            deviceList = new AudioDeviceList(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, guiTop + font.lineHeight + PADDING).setIcon(getIcon()).setConfigEntry(getConfigEntry());
        }
        deviceList.setAudioDevices(getNames());
        addWidget(deviceList);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public abstract Screen getNextScreen();

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        deviceList.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTitle(guiGraphics, title);
    }
}
