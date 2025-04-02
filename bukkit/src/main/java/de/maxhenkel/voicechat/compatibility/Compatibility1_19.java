package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_19 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_19 = BukkitVersion.parseBukkitVersion("1.19-R0.1");

    public static final Compatibility1_19 INSTANCE = new Compatibility1_19();

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
        return Compatibility1_19_1.INSTANCE.playerArgument();
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return Compatibility1_19_1.INSTANCE.uuidArgument();
    }

    private void send(Player player, String json, boolean status) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClazz("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getBukkitClass("util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClazz("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClazz("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, int.class}, iChatBaseComponent, getId(player, status));

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

    private int getId(Player player, boolean status) {
        Object world = callMethod(player.getWorld(), "getHandle");
        Class<?> iRegistryCustomClass = getClazz("net.minecraft.core.IRegistryCustom");
        Object iRegistryCustom = callMethod(world, "s");

        Class<?> resourceKeyClass = getClazz("net.minecraft.resources.ResourceKey");

        Class<?> iRegistry = getClazz("net.minecraft.core.IRegistry");
        Object resourceKey = getField(iRegistry, "bI");

        Object registry = callMethod(iRegistryCustomClass, iRegistryCustom, "d", new Class[]{resourceKeyClass}, resourceKey);

        Class<?> chatMessageTypeClass = getClazz("net.minecraft.network.chat.ChatMessageType");
        Object chatMessageType;
        if (status) {
            chatMessageType = getField(chatMessageTypeClass, "d");
        } else {
            chatMessageType = getField(chatMessageTypeClass, "c");
        }

        Object messageType = callMethod(registry, "a", new Class[]{resourceKeyClass}, chatMessageType);

        return callMethod(registry, "a", new Class[]{Object.class}, messageType);
    }

}
