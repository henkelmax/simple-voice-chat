package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.stream.Collectors;

public class AdjustVolumeScreen extends Screen {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_adjust_volume.png");

    private int guiLeft;
    private int guiTop;
    private int xSize;
    private int ySize;

    private List<PlayerEntity> players;
    private int index;

    private Button previous;
    private Button back;
    private Button next;

    public AdjustVolumeScreen() {
        super(new TranslationTextComponent("gui.adjust_volume.title"));
        xSize = 248;
        ySize = 84;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        players = minecraft.world.getPlayers().stream().map(player -> (PlayerEntity) player).filter(playerEntity -> !playerEntity.equals(minecraft.player)).collect(Collectors.toList()); //TODO all players

        previous = new Button(guiLeft + 10, guiTop + 60, 60, 20, new TranslationTextComponent("message.previous").getFormattedText(), button -> {
            index = (index - 1 + players.size()) % players.size();
            updatePlayer();
        });

        back = new Button(guiLeft + xSize / 2 - 30, guiTop + 60, 60, 20, new TranslationTextComponent("message.back").getFormattedText(), button -> {
            minecraft.displayGuiScreen(new VoiceChatScreen());
        });

        next = new Button(guiLeft + xSize - 80, guiTop + 60, 60, 20, new TranslationTextComponent("message.next").getFormattedText(), button -> {
            index = (index + 1) % players.size();
            updatePlayer();
        });

        updatePlayer();
    }

    public void updatePlayer() {
        buttons.clear();
        children.clear();
        addButton(new AdjustVolumeSlider(guiLeft + 10, guiTop + 30, xSize - 20, 20, getCurrentPlayer()));
        addButton(previous);
        addButton(back);
        addButton(next);

        if (players.size() <= 1) {
            next.active = false;
            previous.active = false;
        }
    }

    public PlayerEntity getCurrentPlayer() {
        if (players.size() <= 0) {
            return null;
        }
        return players.get(index);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == minecraft.gameSettings.keyBindInventory.getKey().getKeyCode() || keyCode == Main.KEY_VOICE_CHAT_SETTINGS.getKey().getKeyCode()) {
            minecraft.displayGuiScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(mouseX, mouseY, partialTicks);

        // Title
        String title = getCurrentPlayer() == null ? new TranslationTextComponent("message.no_player").getFormattedText() : new TranslationTextComponent("message.adjust_volume_player", getCurrentPlayer().getDisplayName()).getFormattedText();
        int titleWidth = font.getStringWidth(title);
        font.drawString(title, (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
