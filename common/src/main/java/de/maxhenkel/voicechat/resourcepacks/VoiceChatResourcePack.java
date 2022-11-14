package de.maxhenkel.voicechat.resourcepacks;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class VoiceChatResourcePack implements IResourcePack {

    protected String path;
    protected ITextComponent name;

    public VoiceChatResourcePack(String path, ITextComponent name) {
        this.path = path;
        this.name = name;
    }

    private String getPath() {
        return "/packs/" + path + "/";
    }

    private String getAssetsPath() {
        return "/packs/" + path + "/assets/" + Voicechat.MODID + "/";
    }

    @Nullable
    private InputStream get(String name) {
        return Voicechat.class.getResourceAsStream(getPath() + name);
    }

    @Nullable
    private InputStream getAsset(String name) {
        return Voicechat.class.getResourceAsStream(getAssetsPath() + name);
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        if (!location.getResourceDomain().equals(Voicechat.MODID)) {
            throw new FileNotFoundException("Resource " + location + " does not exist");
        }
        InputStream resourceAsStream = getAsset(location.getResourcePath());
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Resource " + location + " does not exist");
        }
        return resourceAsStream;
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        if (!location.getResourceDomain().equals(Voicechat.MODID)) {
            return false;
        }
        try {
            return getAsset(location.getResourcePath()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Set<String> getResourceDomains() {
        return ImmutableSet.of(Voicechat.MODID);
    }

    private static <T extends IMetadataSection> T readMetadata(MetadataSerializer metadataSerializer, InputStream inputStream, String sectionName) {
        JsonObject jsonobject;
        BufferedReader bufferedreader = null;
        try {
            bufferedreader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
        } catch (RuntimeException runtimeexception) {
            throw new JsonParseException(runtimeexception);
        } finally {
            IOUtils.closeQuietly(bufferedreader);
        }
        return metadataSerializer.parseMetadataSection(sectionName, jsonobject);
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        InputStream inputStream = get("pack.mcmeta");
        if (inputStream == null) {
            throw new FileNotFoundException("pack.mcmeta does not exist");
        }
        return readMetadata(metadataSerializer, inputStream, metadataSectionName);
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        InputStream inputStream = get("pack.png");
        if (inputStream == null) {
            throw new FileNotFoundException("pack.png does not exist");
        }
        return TextureUtil.readBufferedImage(inputStream);
    }

    @Override
    public String getPackName() {
        return name.getFormattedText();
    }

}
