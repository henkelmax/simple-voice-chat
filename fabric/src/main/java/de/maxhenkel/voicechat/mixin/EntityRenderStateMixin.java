package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.interfaces.RenderStateUuid;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(value = EntityRenderState.class)
public class EntityRenderStateMixin implements RenderStateUuid {

    @Unique
    private UUID uuid;

    @Override
    public UUID voicechat$getUuid() {
        return uuid;
    }

    @Override
    public void voicechat$setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
