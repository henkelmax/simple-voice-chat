package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import org.bukkit.Location;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PositionImpl other = (PositionImpl) object;
        return Double.compare(getX(), other.getX()) == 0 && Double.compare(getY(), other.getY()) == 0 && Double.compare(getZ(), other.getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ());
    }
}
