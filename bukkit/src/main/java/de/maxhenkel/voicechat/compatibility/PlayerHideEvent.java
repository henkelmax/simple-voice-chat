package de.maxhenkel.voicechat.compatibility;

import org.bukkit.entity.Player;

public class PlayerHideEvent {

    private final Player hiddenPlayer;
    private final Player observer;

    public PlayerHideEvent(Player hiddenPlayer, Player observer) {
        this.hiddenPlayer = hiddenPlayer;
        this.observer = observer;
    }

    public Player getHiddenPlayer() {
        return hiddenPlayer;
    }

    public Player getObserver() {
        return observer;
    }

}
