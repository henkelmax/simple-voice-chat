package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(PlayerEntry.class)
public class PlayerEntryMixin {

    private static final ResourceLocation GROUP_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/invite_button.png");

    @Shadow
    @Nullable
    private Button hideButton;
    @Shadow
    @Nullable
    private Button reportButton;
    @Shadow
    @Final
    private String playerName;
    @Shadow
    @Final
    private UUID id;

    @Shadow
    @Final
    private List<AbstractWidget> children;

    private ImageButton inviteButton;
    private boolean invited;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onCreate(Minecraft minecraft, SocialInteractionsScreen socialInteractionsScreen, UUID uUID, String string, Supplier<ResourceLocation> supplier, boolean bl, CallbackInfo ci) {
        if (this.children instanceof ArrayList) {
            inviteButton = new ImageButton(0, 0, GROUP_ICON, button -> {
                minecraft.player.connection.sendUnsignedCommand("voicechat invite %s".formatted(playerName));
                invited = true;
            });
            inviteButton.setTooltip(Tooltip.create(Component.translatable("message.voicechat.invite_player", playerName)));
            inviteButton.setTooltipDelay(10);
            this.children.add(inviteButton);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", ordinal = 1))
    private void render(GuiGraphics poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci) {
        if (inviteButton != null && hideButton != null && reportButton != null) {
            if (ClientManager.getPlayerStateManager().getGroupID() == null || !canInvite()) {
                inviteButton.visible = false;
                return;
            }
            inviteButton.visible = true;
            inviteButton.active = !invited;
            inviteButton.setPosition(left + (width - hideButton.getWidth() - 4 - reportButton.getWidth() - 4) - inviteButton.getWidth() - 4, top + (height - inviteButton.getHeight()) / 2);
            inviteButton.render(poseStack, mouseX, mouseY, delta);
        }
    }

    private boolean canInvite() {
        PlayerState state = ClientManager.getPlayerStateManager().getState(id);
        if (state == null) {
            return false;
        }
        return !state.hasGroup();
    }

}
