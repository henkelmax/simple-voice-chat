package de.maxhenkel.voicechat.macos;

import com.sun.jna.Platform;

import java.awt.*;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static String version;

    public static void main(String[] args) {
        System.out.printf("MacOS patcher version %s%n", getVersion());

        if (!Platform.isMac()) {
            System.out.println("You are not on MacOS");
            System.exit(0);
            return;
        }

        if (args.length > 0) {
            if (args[0].equals("gui")) {
                showGui();
            } else {
                request();
            }
        } else {
            request();
        }
    }

    private static void request() {
        PermissionCheck.AVAuthorizationStatus status = PermissionCheck.getMicrophonePermissions();
        if (status.equals(PermissionCheck.AVAuthorizationStatus.NOT_DETERMINED)) {
            PermissionCheck.requestMicrophonePermissions();
        }

        int i = 0;
        while (status.equals(PermissionCheck.AVAuthorizationStatus.NOT_DETERMINED)) {
            status = PermissionCheck.getMicrophonePermissions();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (i < 10) {
                i++;
            } else {
                System.exit(99);
                break;
            }
        }

        if (status != PermissionCheck.AVAuthorizationStatus.AUTHORIZED) {
            System.err.printf("Simple Voice Chat is unable to use the Microphone. Status: %s%n", status);
        }
    }

    public static void showGui() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("This application does not work in headless mode");
            System.exit(5);
            return;
        }

        new MacosFrame();
    }

    private static String loadVersion() {
        try {
            InputStream in = Main.class.getClassLoader().getResourceAsStream("patcher.properties");
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("patcher_version");
        } catch (Exception e) {
            System.err.printf("Failed to load version: %s%n", e.getMessage());
            return "N/A";
        }
    }

    public static String getVersion() {
        if (version == null) {
            version = loadVersion();
        }
        return version;
    }

}
