package de.maxhenkel.voicechat.dialstuff;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.plugins.impl.ServerLevelImpl;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MinimalPlayer {
    UUID uuid;
    String name;
    boolean spectator;
    de.maxhenkel.voicechat.api.ServerPlayer camera;
    Vec3 pos;
    Vec3 eyePos;

    MinimalPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        spectator = false;
        camera = null;
        pos = Vec3.ZERO;
        eyePos = Vec3.ZERO;
    }
    public ServerGamePacketListenerImpl connection;

    public static de.maxhenkel.voicechat.api.ServerPlayer of(ServerPlayer source) {
        return new ServerPlayerImpl(source);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void displayClientMessage(MutableComponent translatable, boolean b) {
    }

    public boolean isSpectator() {
        return spectator;
    }

    public de.maxhenkel.voicechat.api.ServerPlayer getCamera() {
        return camera;
    }

    public Vec3 getEyePosition() {
        return eyePos;
    }

    public Vec3 position() {
        return pos;
    }
}
