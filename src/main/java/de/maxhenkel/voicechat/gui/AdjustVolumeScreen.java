package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
    protected void func_231160_c_() {
        super.func_231160_c_();
        this.guiLeft = (field_230708_k_ - this.xSize) / 2;
        this.guiTop = (field_230709_l_ - this.ySize) / 2;

        players = field_230706_i_.world.getPlayers().stream().map(player -> (PlayerEntity) player).filter(playerEntity -> !playerEntity.equals(field_230706_i_.player)).collect(Collectors.toList()); //TODO all players

        previous = new Button(guiLeft + 10, guiTop + 60, 60, 20, new TranslationTextComponent("message.previous"), button -> {
            index = (index - 1 + players.size()) % players.size();
            updatePlayer();
        });

        back = new Button(guiLeft + xSize / 2 - 30, guiTop + 60, 60, 20, new TranslationTextComponent("message.back"), button -> {
            field_230706_i_.displayGuiScreen(new VoiceChatScreen());
        });

        next = new Button(guiLeft + xSize - 80, guiTop + 60, 60, 20, new TranslationTextComponent("message.next"), button -> {
            index = (index + 1) % players.size();
            updatePlayer();
        });

        updatePlayer();
    }

    public void updatePlayer() {
        field_230710_m_.clear();
        func_230480_a_(new AdjustVolumeSlider(guiLeft + 10, guiTop + 30, xSize - 20, 20, getCurrentPlayer()));
        func_230480_a_(previous);
        func_230480_a_(back);
        func_230480_a_(next);

        if (players.size() <= 1) {
            next.field_230694_p_ = false;
            previous.field_230694_p_ = false;
        }
    }

    public PlayerEntity getCurrentPlayer() {
        if (players.size() <= 0) {
            return null;
        }
        return players.get(index);
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == field_230706_i_.gameSettings.keyBindInventory.getKey().getKeyCode() || keyCode == Main.KEY_VOICE_CHAT_SETTINGS.getKey().getKeyCode()) {
            field_230706_i_.displayGuiScreen(null);
            return true;
        }
        return super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public void func_230430_a_(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        field_230706_i_.getTextureManager().bindTexture(TEXTURE);
        func_238474_b_(stack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.func_230430_a_(stack, mouseX, mouseY, partialTicks);

        // Title
        ITextComponent title = getCurrentPlayer() == null ? new TranslationTextComponent("message.no_player") : new TranslationTextComponent("message.adjust_volume_player", getCurrentPlayer().getDisplayName());
        int titleWidth = field_230712_o_.getStringWidth(title.getString());
        field_230712_o_.func_238422_b_(stack, title.func_241878_f(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
