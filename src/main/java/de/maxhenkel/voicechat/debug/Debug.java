package de.maxhenkel.voicechat.debug;

import de.maxhenkel.voicechat.Voicechat;

import java.io.*;

public class Debug {

    public static ByteArrayOutputStream buffer;

    public static void writeDebugAudio(byte[] data) {
        if (buffer == null) {
            buffer = new ByteArrayOutputStream(100_000);
        }

        try {
            buffer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveDebugAudio() {
        try {
            File tempFile = File.createTempFile("test", ".sound");
            Voicechat.LOGGER.info(tempFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(tempFile, false);
            buffer.writeTo(fos);
            buffer.close();
            buffer = null;
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
