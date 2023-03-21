package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.plugins.impl.audio.AudioConverterImpl;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3DecoderImpl;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Nullable
    @Override
    public Mp3Encoder createMp3Encoder(AudioFormat audioFormat, int bitrate, int quality, OutputStream outputStream) {
        return Mp3EncoderImpl.createEncoder(audioFormat, bitrate, quality, outputStream);
    }

    @Nullable
    @Override
    public Mp3Decoder createMp3Decoder(InputStream inputStream) {
        return Mp3DecoderImpl.createDecoder(inputStream);
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
        if (entity instanceof net.minecraft.world.entity.Entity e) {
            return new EntityImpl(e);
        } else {
            throw new IllegalArgumentException("entity is not an instance of Entity");
        }
    }

    @Override
    public ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof net.minecraft.server.level.ServerLevel l) {
            return new ServerLevelImpl(l);
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of ServerLevel");
        }
    }

    @Override
    public ServerPlayer fromServerPlayer(Object serverPlayer) {
        if (serverPlayer instanceof net.minecraft.server.level.ServerPlayer p) {
            return new ServerPlayerImpl(p);
        } else {
            throw new IllegalArgumentException("serverPlayer is not an instance of ServerPlayer");
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
