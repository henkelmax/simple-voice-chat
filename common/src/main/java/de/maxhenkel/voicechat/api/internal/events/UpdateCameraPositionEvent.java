package de.maxhenkel.voicechat.api.internal.events;

import de.maxhenkel.voicechat.api.events.ClientEvent;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface UpdateCameraPositionEvent extends ClientEvent {

    void setCameraPosition(Vec3 pos, Vector3fc forward, Vector3fc up);

}
