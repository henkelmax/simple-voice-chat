package de.maxhenkel.voicechat.voice.client;

import org.lwjgl.openal.*;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundManager {

    private static final Pattern DEVICE_NAME = Pattern.compile("^(?:OpenAL.+?on )?(.*)$");

    public static String cleanDeviceName(String name) {
        Matcher matcher = DEVICE_NAME.matcher(name);
        if (!matcher.matches()) {
            return name;
        }
        return matcher.group(1);
    }

    public static List<String> getAllSpeakers() {
        //TODO Fix audio devices
        return Collections.emptyList();
    }

}