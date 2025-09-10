package de.maxhenkel.voicechat.command;

public interface CommandSender {

    void sendMessage(String message);

    boolean hasPermission(String permission);

}
