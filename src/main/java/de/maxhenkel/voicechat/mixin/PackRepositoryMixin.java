package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin implements IPackRepository {

    @Shadow
    @Final
    private Set<RepositorySource> sources;

    @Override
    public void addSource(RepositorySource source) {
        sources.add(source);
    }
}
