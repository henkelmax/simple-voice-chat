package de.maxhenkel.voicechat.resourcepacks;

import com.google.common.collect.ImmutableSet;
import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class VoiceChatResourcePack extends AbstractPackResources {

    public VoiceChatResourcePack(String id) {
        super(id);
    }

    public Pack toPack(Component name) {
        Pack.ResourcesSupplier resourcesSupplier = (s) -> this;
        Pack.Info info = Pack.readPackInfo("", resourcesSupplier);
        if (info == null) {
            info = new Pack.Info(name, PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()), FeatureFlagSet.of(FeatureFlags.VANILLA));
        }
        return Pack.create(this.packId(), name, false, resourcesSupplier, info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, false, PackSource.BUILT_IN);
    }

    private String getPath() {
        return "/packs/" + packId() + "/";
    }

    @Nullable
    private InputStream get(String name) {
        return Voicechat.class.getResourceAsStream(getPath() + name);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... strings) {
        return getResource(String.join("/", strings));
    }

    private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return getResource(getPathFromLocation(packType, resourceLocation));
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String path) {
        InputStream resourceAsStream = get(path);
        if (resourceAsStream == null) {
            return null;
        }
        return () -> resourceAsStream;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput resourceOutput) {
        try {
            URL url = Voicechat.class.getResource(getPath());
            if (url == null) {
                return;
            }
            Path resPath = Paths.get(url.toURI());

            String absolutePath = type.getDirectory() + "/" + namespace + "/";
            String absolutePrefixPath = absolutePath + prefix + "/";

            try (Stream<Path> files = Files.walk(resPath)) {
                files.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    String name = path.getFileName().toString();
                    if (!name.endsWith(".mcmeta") && name.startsWith(absolutePrefixPath)) {
                        ResourceLocation resourceLocation = new ResourceLocation(namespace, name.substring(absolutePath.length()));
                        resourceOutput.accept(resourceLocation, getResource(type, resourceLocation));
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType == PackType.CLIENT_RESOURCES) {
            return ImmutableSet.of(Voicechat.MODID);
        }
        return ImmutableSet.of();
    }

    @Override
    public void close() {

    }
}
