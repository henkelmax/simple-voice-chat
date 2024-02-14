package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;

public abstract class DeviceOnboardingScreen extends OnboardingScreenBase {

    protected AudioDeviceList deviceList;

    protected List<String> micNames;

    public DeviceOnboardingScreen(ITextComponent title, @Nullable GuiScreen previous) {
        super(title, previous);
        mc = Minecraft.getMinecraft();
        micNames = getNames();
        if (micNames.isEmpty()) {
            mc.addScheduledTask(() -> mc.displayGuiScreen(getNextScreen()));
        }
    }

    public abstract List<String> getNames();

    public abstract ResourceLocation getIcon();

    public abstract ConfigEntry<String> getConfigEntry();

    @Override
    public void initGui() {
        super.initGui();

        deviceList = new AudioDeviceList(width, contentHeight - fontRenderer.FONT_HEIGHT - BUTTON_HEIGHT - PADDING * 2, guiTop + fontRenderer.FONT_HEIGHT + PADDING).setIcon(getIcon()).setConfigEntry(getConfigEntry());

        deviceList.setAudioDevices(getNames());
        setList(deviceList);

        addBackOrCancelButton(1);
        addNextButton(0);
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
