package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Compatibility1_20_2 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_20_2 = BukkitVersion.parseBukkitVersion("1.20.2-R0.1");

    public static final Compatibility1_20_2 INSTANCE = new Compatibility1_20_2();

    @Override
    public String getServerIp(Server server) throws Exception {
        Object dedicatedServer = callMethod(server, "getServer");
        Object dedicatedServerProperties = callMethod(dedicatedServer, "a");
        return getField(dedicatedServerProperties, "c");
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
        Class<?> argumentEntity = getClass("net.minecraft.commands.arguments.ArgumentEntity");
        return callMethod(argumentEntity, "c");
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        Class<?> argumentEntity = getClass("net.minecraft.commands.arguments.ArgumentUUID");
        return callMethod(argumentEntity, "a");
    }

    private void send(Player player, Component component, boolean status) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "c");
        Class<?> packet = getClass("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getClass("org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, boolean.class}, iChatBaseComponent, status);

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
