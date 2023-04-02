package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Compatibility1_19 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_19_4 = BukkitVersion.parseBukkitVersion("1.19.4-R0.1");
    public static final BukkitVersion VERSION_1_19_3 = BukkitVersion.parseBukkitVersion("1.19.3-R0.1");
    public static final BukkitVersion VERSION_1_19_2 = BukkitVersion.parseBukkitVersion("1.19.2-R0.1");
    public static final BukkitVersion VERSION_1_19_1 = BukkitVersion.parseBukkitVersion("1.19.1-R0.1");
    public static final BukkitVersion VERSION_1_19 = BukkitVersion.parseBukkitVersion("1.19-R0.1");

    public static final Compatibility1_19 INSTANCE = new Compatibility1_19();

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

    private void send(Player player, Component component, boolean status) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClass("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getClass(
                "org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage",
                "org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage",
                "org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage"
        );

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> clientboundSystemChatPacketClass = getClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

        Object clientboundSystemChatPacket = callConstructor(clientboundSystemChatPacketClass, new Class[]{iChatBaseComponentClass, boolean.class}, iChatBaseComponent, status);

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
