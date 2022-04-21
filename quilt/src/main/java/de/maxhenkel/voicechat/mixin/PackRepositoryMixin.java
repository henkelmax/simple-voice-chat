package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = PackRepository.class, priority = 0)
public class PackRepositoryMixin implements IPackRepository {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Override
    public void addSource(RepositorySource source) {
        Set<RepositorySource> set = new HashSet<>(sources);
        set.add(source);
        sources = set;
    }
}
