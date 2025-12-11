package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class PositionImpl implements Position {

    private final Vec3 position;

    public PositionImpl(Vec3 position) {
        this.position = position;
    }

    public PositionImpl(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
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

    @Override
    public Vec3 getVec3() {
        return position;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Position position1)) {
            return false;
        }
        return Objects.equals(position, position1.getVec3());
    }

    @Override
    public int hashCode() {
        return position != null ? position.hashCode() : 0;
    }
}
