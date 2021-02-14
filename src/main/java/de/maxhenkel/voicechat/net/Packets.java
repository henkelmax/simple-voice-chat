package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.util.Identifier;

public class Packets {

    public static final Identifier SECRET = new Identifier(Voicechat.MODID, "secret");
    public static final Identifier REQUEST_PLAYER_LIST = new Identifier(Voicechat.MODID, "request_player_list");
    public static final Identifier PLAYER_LIST = new Identifier(Voicechat.MODID, "player_list");

}
