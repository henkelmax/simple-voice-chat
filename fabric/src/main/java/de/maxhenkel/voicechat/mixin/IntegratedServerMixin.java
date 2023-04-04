package de.maxhenkel.voicechat.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import de.maxhenkel.voicechat.events.PublishServerEvents;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.GameType;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {


    public IntegratedServerMixin(Thread thread, DynamicRegistries.Impl impl, SaveFormat.LevelSave levelSave, IServerConfiguration iServerConfiguration, ResourcePackList resourcePackList, Proxy proxy, DataFixer dataFixer, DataPackRegistries dataPackRegistries, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, PlayerProfileCache playerProfileCache, IChunkStatusListenerFactory iChunkStatusListenerFactory) {
        super(thread, impl, levelSave, iServerConfiguration, resourcePackList, proxy, dataFixer, dataPackRegistries, minecraftSessionService, gameProfileRepository, playerProfileCache, iChunkStatusListenerFactory);
    }

    @Inject(method = "publishServer", at = @At(value = "RETURN"))
    public void publishServer(@Nullable GameType gameType, boolean cheats, int port, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!callbackInfo.getReturnValue()) {
            return;
        }
        PublishServerEvents.SERVER_PUBLISHED.invoker().accept(port);
    }

}
