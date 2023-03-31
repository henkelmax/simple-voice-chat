package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class NetManager implements Listener {

    private final Set<String> packets = new HashSet<>();

    public void onEnable() {
        packets.clear();
        Bukkit.getPluginManager().registerEvents(this, Voicechat.INSTANCE);
        try {
            registerIncomingPacket(UpdateStatePacket.class);
            registerIncomingPacket(RequestSecretPacket.class);
            registerIncomingPacket(CreateGroupPacket.class);
            registerIncomingPacket(JoinGroupPacket.class);
            registerIncomingPacket(LeaveGroupPacket.class);

            registerOutgoingPacket(SecretPacket.class);
            registerOutgoingPacket(PlayerStatesPacket.class);
            registerOutgoingPacket(PlayerStatePacket.class);
            registerOutgoingPacket(AddGroupPacket.class);
            registerOutgoingPacket(RemoveGroupPacket.class);
            registerOutgoingPacket(JoinedGroupPacket.class);
            registerOutgoingPacket(AddCategoryPacket.class);
            registerOutgoingPacket(RemoveCategoryPacket.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register voice chat packets");
        }
    }

    public void onDisable() {
        Set<String> incomingChannels = Bukkit.getMessenger().getIncomingChannels(Voicechat.INSTANCE);
        Set<String> outgoingChannels = Bukkit.getMessenger().getOutgoingChannels(Voicechat.INSTANCE);
        for (String channel : incomingChannels) {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(Voicechat.INSTANCE, channel);
        }
        for (String channel : outgoingChannels) {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(Voicechat.INSTANCE, channel);
        }
        packets.clear();
    }

    public Set<String> getPackets() {
        return packets;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() instanceof CraftPlayer player) {
            Set<String> outgoingChannels = Bukkit.getMessenger().getOutgoingChannels(Voicechat.INSTANCE);
            for (String channel : outgoingChannels) {
                player.addChannel(channel);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() instanceof CraftPlayer player) {
            Set<String> outgoingChannels = Bukkit.getMessenger().getOutgoingChannels(Voicechat.INSTANCE);
            for (String channel : outgoingChannels) {
                player.removeChannel(channel);
            }
        }
    }

    private <T extends Packet<?>> void registerIncomingPacket(Class<T> packetClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> c = packetClass.getDeclaredConstructor();
        String id = c.newInstance().getID().toString();
        packets.add(id);
        Bukkit.getMessenger().registerIncomingPluginChannel(Voicechat.INSTANCE, id, (s, player, bytes) -> {
            T packet;
            try {
                packet = c.newInstance();
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to create new packet instance of {}: {}", packetClass.getSimpleName(), e.getMessage());
                return;
            }
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            packet.fromBytes(buffer);
            packet.onPacket(player);
        });
    }

    private <T extends Packet<?>> void registerOutgoingPacket(Class<T> packetClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String id = packetClass.getDeclaredConstructor().newInstance().getID().toString();
        packets.add(id);
        Bukkit.getMessenger().registerOutgoingPluginChannel(Voicechat.INSTANCE, id);
    }

    public static void sendToClient(Player player, Packet<?> p) {
        Bukkit.getGlobalRegionScheduler().run(Voicechat.INSTANCE, (task) -> {
            if (!Voicechat.SERVER.isCompatible(player)) {
                return;
            }
            try {
                FriendlyByteBuf buf = new FriendlyByteBuf();
                p.toBytes(buf);
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                player.sendPluginMessage(Voicechat.INSTANCE, p.getID().toString(), bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendMessage(Player p, Component component) {
        if (p instanceof CraftPlayer player) {
            player.getHandle().b.a(new ClientboundSystemChatPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)), false));
        }
    }

    public static void sendStatusMessage(Player p, Component component) {
        if (p instanceof CraftPlayer player) {
            player.getHandle().b.a(new ClientboundSystemChatPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)), true));
        }
    }

}