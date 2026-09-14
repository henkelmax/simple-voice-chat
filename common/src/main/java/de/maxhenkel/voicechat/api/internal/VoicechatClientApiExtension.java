package de.maxhenkel.voicechat.api.internal;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface VoicechatClientApiExtension {

    void updateAudioLevel(UUID id, @Nullable String category, boolean whispering, short[] audio);

    UUID getOwnId();

}
