package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    protected List<String> micNames;

    public DeviceOnboardingScreen(ITextComponent title, @Nullable Screen previous) {
        super(title, previous);
        minecraft = Minecraft.getInstance();
        micNames = getNames();
        if (micNames.isEmpty()) {
            minecraft.tell(() -> minecraft.setScreen(getNextScreen()));
        }
    }

    public abstract List<String> getNames();

    public abstract ResourceLocation getIcon();

    public abstract ConfigEntry<String> getConfigEntry();

    @Override
    protected void init() {
        super.init();

        if (deviceList != null) {
            deviceList.updateSize(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, guiTop + font.lineHeight + PADDING);
        } else {
            deviceList = new AudioDeviceList(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, guiTop + font.lineHeight + PADDING).setIcon(getIcon()).setConfigEntry(getConfigEntry());
        }
        deviceList.setAudioDevices(getNames());
        addWidget(deviceList);

        addBackOrCancelButton();
        addNextButton();
    }

    @Override
    public abstract Screen getNextScreen();

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        deviceList.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, title);
    }
}
