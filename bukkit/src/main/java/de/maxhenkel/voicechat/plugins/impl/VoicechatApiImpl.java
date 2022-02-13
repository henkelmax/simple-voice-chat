package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.plugins.impl.audio.AudioConverterImpl;
import org.bukkit.World;

import javax.annotation.Nullable;

public class VoicechatApiImpl implements VoicechatApi {

    private static final AudioConverter AUDIO_CONVERTER = new AudioConverterImpl();

    @Nullable
    @Override
    public OpusEncoder createEncoder() {
        return null;
    }

    @Nullable
    @Override
    public OpusEncoder createEncoder(OpusEncoderMode mode) {
        return null;
    }

    @Nullable
    @Override
    public OpusDecoder createDecoder() {
        return null;
    }

    public AudioConverter getAudioConverter() {
        return AUDIO_CONVERTER;
    }

    @Override
    public Entity fromEntity(Object entity) {
        if (entity instanceof org.bukkit.entity.Entity) {
            return new EntityImpl((org.bukkit.entity.Entity) entity);
        } else {
            throw new IllegalArgumentException("entity is not an instance of Entity");
        }
    }

    @Override
    public ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof World) {
            return new ServerLevelImpl((World) serverLevel);
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of World");
        }
    }

    @Override
    public ServerPlayer fromServerPlayer(Object serverPlayer) {
        if (serverPlayer instanceof org.bukkit.entity.Player) {
            return new ServerPlayerImpl((org.bukkit.entity.Player) serverPlayer);
        } else {
            throw new IllegalArgumentException("serverPlayer is not an instance of Player");
        }
    }

    @Override
    public Position createPosition(double x, double y, double z) {
        return new PositionImpl(x, y, z);
    }

}
