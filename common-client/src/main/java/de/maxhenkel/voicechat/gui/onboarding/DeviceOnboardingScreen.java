package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    public DeviceOnboardingScreen(ITextComponent title, @Nullable Screen previous) {
        super(title, previous);
        minecraft = Minecraft.getInstance();
    }

    public abstract AudioDeviceList createAudioDeviceList(int width, int height, int top);

    @Override
    protected void init() {
        super.init();

        if (deviceList != null) {
            deviceList.updateSize(width, contentHeight - font.lineHeight - BUTTON_HEIGHT - PADDING * 2, guiTop + font.lineHeight + PADDING);
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
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        deviceList.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, title);
    }
}
