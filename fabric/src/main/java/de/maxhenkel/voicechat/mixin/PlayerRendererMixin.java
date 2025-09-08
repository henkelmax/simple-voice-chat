package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerRenderer.class, priority = 10000)
public abstract class PlayerRendererMixin extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public PlayerRendererMixin(EntityRendererManager entityRendererManager, PlayerModel<AbstractClientPlayerEntity> entityModel, float f) {
        super(entityRendererManager, entityModel, f);
    }

    @Inject(method = "renderNameTag(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/text/ITextComponent;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingRenderer;renderNameTag(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/text/ITextComponent;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", ordinal = 1))
    private void renderNameTag(AbstractClientPlayerEntity player, ITextComponent component, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int light, CallbackInfo info) {
        if (info.isCancelled()) {
            return;
        }
        if (!shouldShowName(player)) {
            return;
        }
        RenderEvents.RENDER_NAMEPLATE.invoker().render(player, component, matrixStack, iRenderTypeBuffer, light);
    }

}