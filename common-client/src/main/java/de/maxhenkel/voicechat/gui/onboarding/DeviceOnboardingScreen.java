package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    public DeviceOnboardingScreen(ITextComponent title, @Nullable GuiScreen previous) {
        super(title, previous);
        mc = Minecraft.getMinecraft();
    }

    public abstract AudioDeviceList createAudioDeviceList(int width, int height, int top);

    @Override
    public void initGui() {
        super.initGui();

        deviceList = createAudioDeviceList(width, contentHeight - fontRenderer.FONT_HEIGHT - BUTTON_HEIGHT - PADDING * 2, guiTop + fontRenderer.FONT_HEIGHT + PADDING);
        setList(deviceList);

        addBackOrCancelButton(0);
        addNextButton(1);
    }

    @Override
    public abstract GuiScreen getNextScreen();

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        super.drawScreen(mouseX, mouseY, delta);
        deviceList.drawScreen(mouseX, mouseY, delta);
        renderTitle(title);
    }
}
