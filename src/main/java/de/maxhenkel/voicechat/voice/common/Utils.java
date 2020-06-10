package de.maxhenkel.voicechat.voice.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }

    public static byte[] gUnzip(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gzipIS = new GZIPInputStream(bis);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipIS.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.flush();
        byte[] decompressed = bos.toByteArray();

        gzipIS.close();
        bos.close();
        bis.close();
        return decompressed;
    }

    public static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzipOut = new GZIPOutputStream(bos);
        gzipOut.write(data);
        gzipOut.flush();
        gzipOut.close();
        bos.flush();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }
}
