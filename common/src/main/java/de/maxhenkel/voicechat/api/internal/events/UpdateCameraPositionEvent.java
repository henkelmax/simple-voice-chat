package de.maxhenkel.voicechat.api.internal.events;

import de.maxhenkel.voicechat.api.events.ClientEvent;
import net.minecraft.util.math.Vec3d;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface UpdateCameraPositionEvent extends ClientEvent {

    void setCameraPosition(Vec3d pos, Vec3d forward, Vec3d up);

}
