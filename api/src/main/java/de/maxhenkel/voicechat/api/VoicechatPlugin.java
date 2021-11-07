package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.resources.ResourceLocation;

public interface VoicechatPlugin {

    /**
     * @return the ID of this plugin - Has to be unique
     */
    ResourceLocation getPluginId();

    /**
     * Called after loading the plugin
     */
    void initialize();

    /**
     * Register your events here - Only here!
     *
     * @param registration the event registration object, used to register events
     */
    void registerEvents(EventRegistration registration);

}
