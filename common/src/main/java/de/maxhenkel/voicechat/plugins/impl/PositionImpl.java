package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import net.minecraft.world.phys.Vec3;

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

    public Vec3 getPosition() {
        return position;
    }
}
