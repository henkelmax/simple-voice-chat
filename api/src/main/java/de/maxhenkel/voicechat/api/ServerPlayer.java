package de.maxhenkel.voicechat.api;

public interface ServerPlayer extends Player {

    boolean isSpectator();

    ServerPlayer getCameraPlayer();
}
