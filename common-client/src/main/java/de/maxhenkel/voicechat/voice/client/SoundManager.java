package de.maxhenkel.voicechat.voice.client;

import org.lwjgl.openal.*;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundManager {

    /*private final Set<Speaker> speakers = new HashSet<>();
    private boolean closing;

    public void trackSpeaker(Speaker speaker) throws SpeakerException {
        synchronized (speakers) {
            if (closing) {
                throw new SpeakerException("Sound manager is closing");
            }
            speakers.add(speaker);
        }
    }

    public void untrackSpeaker(Speaker speaker) {
        synchronized (speakers) {
            speakers.remove(speaker);
        }
    }

    public boolean isClosing() {
        synchronized (speakers) {
            return closing;
        }
    }

    private void closeSpeakers() {
        List<Speaker> toClose;
        synchronized (speakers) {
            closing = true;
            toClose = new ArrayList<>(speakers);
            speakers.clear();
        }
        for (Speaker speaker : toClose) {
            speaker.close();
        }
    }*/

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
