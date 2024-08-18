package de.maxhenkel.voicechat.interfaces;

import javax.annotation.Nullable;
import java.util.UUID;

public interface RenderStateUuid {

    @Nullable
    UUID voicechat$getUuid();

    void voicechat$setUuid(UUID uuid);

}
