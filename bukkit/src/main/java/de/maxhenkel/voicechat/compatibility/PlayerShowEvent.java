package de.maxhenkel.voicechat.compatibility;

import org.bukkit.entity.Player;

public class PlayerShowEvent {

    private final Player shownPlayer;
    private final Player observer;

    public PlayerShowEvent(Player shownPlayer, Player observer) {
        this.shownPlayer = shownPlayer;
        this.observer = observer;
    }

    public Player getShownPlayer() {
        return shownPlayer;
    }

    public Player getObserver() {
        return observer;
    }

}
