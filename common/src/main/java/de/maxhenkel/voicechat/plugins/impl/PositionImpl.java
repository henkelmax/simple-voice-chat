package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Position;
import net.minecraft.util.math.vector.Vector3d;

public class PositionImpl implements Position {

    private final Vector3d position;

    public PositionImpl(Vector3d position) {
        this.position = position;
    }

    public PositionImpl(double x, double y, double z) {
        this.position = new Vector3d(x, y, z);
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

    public Vector3d getPosition() {
        return position;
    }
}
