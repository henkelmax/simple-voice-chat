package de.maxhenkel.voicechat.api;

public interface Position {

    /**
     * @return the actual vec3 object
     */
    Object getVec3();

    /**
     * @return the X position
     */
    double getX();

    /**
     * @return the Y position
     */
    double getY();

    /**
     * @return the Z position
     */
    double getZ();

}
