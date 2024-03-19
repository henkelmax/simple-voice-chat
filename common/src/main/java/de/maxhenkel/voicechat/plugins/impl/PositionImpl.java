package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class PositionImpl implements Position {

    private final Vec3d position;

    public PositionImpl(Vec3d position) {
        this.position = position;
    }

    public PositionImpl(double x, double y, double z) {
        this.position = new Vec3d(x, y, z);
    }

    @Override
    public double getX() {
        return position.x;
    }

    @Override
    public double getY() {
        return position.y;
    }

    @Override
    public double getZ() {
        return position.z;
    }

    public Vec3d getPosition() {
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
        PositionImpl position1 = (PositionImpl) object;
        return Objects.equals(position, position1.position);
    }

    @Override
    public int hashCode() {
        return position != null ? position.hashCode() : 0;
    }
}
