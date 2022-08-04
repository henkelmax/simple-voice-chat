package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.plugins.impl.audio.AudioConverterImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.common.Utils;
import org.bukkit.World;

public abstract class VoicechatApiImpl implements VoicechatApi {

    private static final AudioConverter AUDIO_CONVERTER = new AudioConverterImpl();

    @Override
    public OpusEncoder createEncoder() {
        return OpusManager.createEncoder(null);
    }

    @Override
    public OpusEncoder createEncoder(OpusEncoderMode mode) {
        return OpusManager.createEncoder(mode);
    }

    @Override
    public OpusDecoder createDecoder() {
        return OpusManager.createDecoder();
    }

    public AudioConverter getAudioConverter() {
        return AUDIO_CONVERTER;
    }

    @Override
    public Entity fromEntity(Object entity) {
        if (entity instanceof org.bukkit.entity.Entity e) {
            return new EntityImpl(e);
        } else {
            throw new IllegalArgumentException("entity is not an instance of Entity");
        }
    }

    @Override
    public ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof World l) {
            return new ServerLevelImpl(l);
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of World");
        }
    }

    @Override
    public ServerPlayer fromServerPlayer(Object serverPlayer) {
        if (serverPlayer instanceof org.bukkit.entity.Player p) {
            return new ServerPlayerImpl(p);
        } else {
            throw new IllegalArgumentException("serverPlayer is not an instance of Player");
        }
    }

    @Override
    public Position createPosition(double x, double y, double z) {
        return new PositionImpl(x, y, z);
    }

    @Override
    public VolumeCategory.Builder volumeCategoryBuilder() {
        return new VolumeCategoryImpl.BuilderImpl();
    }

    @Override
    public double getVoiceChatDistance() {
        return Utils.getDefaultDistance();
    }

}
