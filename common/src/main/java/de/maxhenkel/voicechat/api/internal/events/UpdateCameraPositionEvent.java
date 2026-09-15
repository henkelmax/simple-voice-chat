package de.maxhenkel.voicechat.api.internal.events;

import com.mojang.math.Vector3f;import de.maxhenkel.voicechat.api.events.ClientEvent;
import net.minecraft.world.phys.Vec3;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface UpdateCameraPositionEvent extends ClientEvent {

    void setCameraPosition(Vec3 pos, Vector3f forward, Vector3f up);

}
