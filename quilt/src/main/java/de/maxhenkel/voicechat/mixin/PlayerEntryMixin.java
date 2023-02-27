package de.maxhenkel.voicechat.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(PlayerEntry.class)
public class PlayerEntryMixin {

    private static final ResourceLocation GROUP_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/invite_button.png");

    @Shadow
    @Nullable
    private Button hideButton;
    @Shadow
    @Final
    private String playerName;
    @Shadow
    float tooltipHoverTime;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private UUID id;

    private SocialInteractionsScreen screen;
    private ImageButton inviteButton;
    private boolean invited;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(Minecraft minecraft, SocialInteractionsScreen socialInteractionsScreen, UUID uUID, String string, Supplier<ResourceLocation> supplier, CallbackInfo ci) {
        screen = socialInteractionsScreen;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
    private ImmutableList<?> children(Object o1, Object o2) {
        inviteButton = new ImageButton(0, 0, GROUP_ICON, button -> {
            minecraft.player.chat("/voicechat invite %s".formatted(playerName));
            invited = true;
        }, (button, matrices, mouseX, mouseY) -> {
            if (screen == null || invited) {
                return;
            }
            tooltipHoverTime += minecraft.getDeltaFrameTime();
            if (tooltipHoverTime < 10F) {
                return;
            }
            screen.setPostRenderRunnable(() -> {
                screen.renderTooltip(matrices, new TranslatableComponent("message.voicechat.invite_player", playerName), mouseX, mouseY);
                screen.setPostRenderRunnable(null);
            });
        });
        return ImmutableList.of(o1, o2, inviteButton);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", ordinal = 1))
    private void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci) {
        if (inviteButton != null && hideButton != null) {
            if (ClientManager.getPlayerStateManager().getGroupID() == null || !canInvite()) {
                inviteButton.visible = false;
                return;
            }
            inviteButton.visible = true;
            inviteButton.active = !invited;
            inviteButton.x = left + (width - hideButton.getWidth() - 4) - inviteButton.getWidth() - 4;
            inviteButton.y = top + (height - inviteButton.getHeight()) / 2;
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
