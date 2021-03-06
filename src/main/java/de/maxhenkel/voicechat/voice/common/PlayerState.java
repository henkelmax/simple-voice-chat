package de.maxhenkel.voicechat.voice.common;

public class PlayerState {

    private boolean disabled;
    private boolean disconnected;

    public PlayerState(boolean disabled, boolean disconnected) {
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
}
