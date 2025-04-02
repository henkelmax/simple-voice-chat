package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_19_1 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_19_4 = BukkitVersion.parseBukkitVersion("1.19.4-R0.1");
    public static final BukkitVersion VERSION_1_19_3 = BukkitVersion.parseBukkitVersion("1.19.3-R0.1");
    public static final BukkitVersion VERSION_1_19_2 = BukkitVersion.parseBukkitVersion("1.19.2-R0.1");
    public static final BukkitVersion VERSION_1_19_1 = BukkitVersion.parseBukkitVersion("1.19.1-R0.1");

    public static final Compatibility1_19_1 INSTANCE = new Compatibility1_19_1();

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
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClazz("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getBukkitClass("util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClazz("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClazz("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, boolean.class}, iChatBaseComponent, status);

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
