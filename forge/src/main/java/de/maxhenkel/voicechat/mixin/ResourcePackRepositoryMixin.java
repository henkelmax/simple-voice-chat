package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.util.List;

@Mixin(ResourcePackRepository.class)
public class ResourcePackRepositoryMixin {

    @Shadow
    private List<ResourcePackRepository.Entry> repositoryEntriesAll;

    @Inject(method = "updateRepositoryEntriesAll", at = @At("RETURN"))
    public void updateRepositoryEntriesAll(CallbackInfo info) {
        addResourcePack(VoicechatClient.CLASSIC_ICONS);
        addResourcePack(VoicechatClient.WHITE_ICONS);
        addResourcePack(VoicechatClient.BLACK_ICONS);
    }

    private void addResourcePack(IResourcePack resourcePack) {
        ResourcePackRepository.Entry entry = createEntry(resourcePack);
        if (entry == null) {
            return;
        }
        try {
            entry.updateResourcePack();
            repositoryEntriesAll.add(entry);
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to update resource pack", e);
        }
    }

    private ResourcePackRepository.Entry createEntry(IResourcePack resourcePack) {
        try {
            Constructor<ResourcePackRepository.Entry> constructor = ResourcePackRepository.Entry.class.getDeclaredConstructor(ResourcePackRepository.class, IResourcePack.class);
            constructor.setAccessible(true);
            return constructor.newInstance(((ResourcePackRepository) ((Object) this)), resourcePack);
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to create resource pack entry", e);
            return null;
        }
    }

}
