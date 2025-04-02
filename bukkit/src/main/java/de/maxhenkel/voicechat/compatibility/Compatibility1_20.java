package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_20 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_20 = BukkitVersion.parseBukkitVersion("1.20-R0.1");
    public static final BukkitVersion VERSION_1_20_1 = BukkitVersion.parseBukkitVersion("1.20.1-R0.1");

    public static final Compatibility1_20 INSTANCE = new Compatibility1_20();

    @Override
    public void sendJsonMessage(Player player, String json) {
        send(player, json, false);
    }

    @Override
    public void sendJsonStatusMessage(Player player, String json) {
        send(player, json, true);
    }

    @Override
    public String createTranslationMessage(String key, String... args) {
        return Compatibility1_8.constructTranslationMessage(key, args);
    }

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendJsonMessage(player, Compatibility1_8.constructInviteMessage(commandSender, groupName, joinCommand));
    }

    @Override
    public void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName) {
        sendJsonMessage(player, Compatibility1_8.constructIncompatibleMessage(pluginVersion, pluginName));
    }

    @Override
    public ArgumentType<?> playerArgument() {
        Class<?> argumentEntity = getClazz("net.minecraft.commands.arguments.ArgumentEntity");
        return callMethod(argumentEntity, "c");
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        Class<?> argumentEntity = getClazz("net.minecraft.commands.arguments.ArgumentUUID");
        return callMethod(argumentEntity, "a");
    }

    private void send(Player player, String json, boolean status) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "c");
        Class<?> packet = getClazz("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getBukkitClass("util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClazz("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClazz("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, boolean.class}, iChatBaseComponent, status);

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
