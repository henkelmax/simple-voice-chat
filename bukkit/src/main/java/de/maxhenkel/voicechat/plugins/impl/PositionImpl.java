package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import org.bukkit.Location;

public class PositionImpl implements Position {

    private final Location position;

    public PositionImpl(Location position) {
        this.position = position;
    }

    public PositionImpl(double x, double y, double z) {
        this.position = new Location(null, x, y, z);
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public double getZ() {
        return position.getZ();
    }

    public Location getPosition() {
        return position;
    }
}
