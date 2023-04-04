package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.IPackFinder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = ResourcePackList.class, priority = 0)
public class PackRepositoryMixin implements IPackRepository {

    @Shadow
    @Final
    @Mutable
    private Set<IPackFinder> sources;

    @Override
    public void addSource(IPackFinder source) {
        Set<IPackFinder> set = new HashSet<>(sources);
        set.add(source);
        sources = set;
    }
}
