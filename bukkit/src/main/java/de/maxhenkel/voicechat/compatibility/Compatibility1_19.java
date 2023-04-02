package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Compatibility1_19 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_19 = BukkitVersion.parseBukkitVersion("1.19-R0.1");

    public static final Compatibility1_19 INSTANCE = new Compatibility1_19();

    @Override
    public String getServerIp(Server server) throws Exception {
        return Compatibility1_19_1.INSTANCE.getServerIp(server);
    }

    @Override
    public void sendMessage(Player player, Component component) {
        send(player, component, false);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        send(player, component, true);
    }

    @Override
    public ArgumentType<?> playerArgument() {
        return Compatibility1_19_1.INSTANCE.playerArgument();
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return Compatibility1_19_1.INSTANCE.uuidArgument();
    }

    private void send(Player player, Component component, boolean status) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClass("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getClass("org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, int.class}, iChatBaseComponent, getId(player, status));

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

    private int getId(Player player, boolean status) {
        Object world = callMethod(player.getWorld(), "getHandle");
        Class<?> iRegistryCustomClass = getClass("net.minecraft.core.IRegistryCustom");
        Object iRegistryCustom = callMethod(world, "s");

        Class<?> resourceKeyClass = getClass("net.minecraft.resources.ResourceKey");

        Class<?> iRegistry = getClass("net.minecraft.core.IRegistry");
        Object resourceKey = getField(iRegistry, "bI");

        Object registry = callMethod(iRegistryCustomClass, iRegistryCustom, "d", new Class[]{resourceKeyClass}, resourceKey);

        Class<?> chatMessageTypeClass = getClass("net.minecraft.network.chat.ChatMessageType");
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
