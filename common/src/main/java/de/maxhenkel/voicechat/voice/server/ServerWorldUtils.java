package de.maxhenkel.voicechat.voice.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ServerWorldUtils {

    public static Collection<EntityPlayerMP> getPlayersInRange(WorldServer level, Vec3d pos, double range, Predicate<EntityPlayerMP> filter) {
        List<EntityPlayerMP> nearbyPlayers = new ArrayList<>();
        List<EntityPlayer> players = level.playerEntities;
        for (int i = 0; i < players.size(); i++) {
            EntityPlayerMP player = (EntityPlayerMP) players.get(i);
            if (isInRange(player.getPositionVector(), pos, range) && filter.test(player)) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }

    public static boolean isInRange(Vec3d pos1, Vec3d pos2, double range) {
        return pos1.squareDistanceTo(pos2) <= range * range;
    }

}
