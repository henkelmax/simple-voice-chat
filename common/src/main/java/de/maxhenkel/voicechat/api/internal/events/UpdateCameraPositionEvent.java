package de.maxhenkel.voicechat.api.internal.events;

import de.maxhenkel.voicechat.api.events.ClientEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface UpdateCameraPositionEvent extends ClientEvent {

    void setCameraPosition(Vector3d pos, Vector3f forward, Vector3f up);

}
