package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.CreateGroupList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = new Identifier(Voicechat.MODID, "textures/gui/gui_create_group.png");

    public static final Pattern groupRegex = Pattern.compile("^[a-zA-Z0-9-_]*$");

    private CreateGroupList playerList;
    private TextFieldWidget groupName;
    private ButtonWidget createGroup;

    public CreateGroupScreen() {
        super(new TranslatableText("gui.voicechat.join_create_group.title"), 195, 146);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();
        client.keyboard.setRepeatEvents(true);

        playerList = new CreateGroupList(this, 9, 49, 160, 88, () -> VoicechatClient.CLIENT.getPlayerStateManager().getPlayerStates());

        groupName = new TextFieldWidget(textRenderer, guiLeft + 78, guiTop + 20, 88, 10, LiteralText.EMPTY);

        groupName.setMaxLength(16);
        groupName.setTextPredicate(s -> groupRegex.matcher(s).matches());

        addButton(groupName);

        createGroup = new ButtonWidget(guiLeft + 169, guiTop + 15, 20, 20, new LiteralText("+"), button -> {
            VoicechatClient.CLIENT.getPlayerStateManager().setGroup(groupName.getText());
            client.openScreen(new GroupScreen());
        });
        addButton(createGroup);
    }

    @Override
    public void tick() {
        super.tick();
        groupName.tick();
        createGroup.active = !groupName.getText().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();
        client.keyboard.setRepeatEvents(false);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        playerList.drawGuiContainerBackgroundLayer(matrixStack, delta, mouseX, mouseY);

        playerList.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        for (AbstractButtonWidget widget : buttons) {
            widget.render(matrixStack, mouseX, mouseY, delta);
        }

        textRenderer.draw(matrixStack, new TranslatableText("message.voicechat.join_create_group"), guiLeft + 8, guiTop + 5, FONT_COLOR);
        textRenderer.draw(matrixStack, new TranslatableText("message.voicechat.group_name"), guiLeft + 8, guiTop + 21, FONT_COLOR);
        textRenderer.draw(matrixStack, new TranslatableText("message.voicechat.join_group"), guiLeft + 8, guiTop + 38, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.player.closeScreen();
            return true;
        }

        return groupName.keyPressed(keyCode, scanCode, modifiers)
                || groupName.isActive()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (playerList.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (playerList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playerList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String groupNameText = groupName.getText();
        init(client, width, height);
        groupName.setText(groupNameText);
    }

}
