package de.maxhenkel.voicechat.macos;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.macos.jna.avfoundation.AVAuthorizationStatus;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
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
        AVAuthorizationStatus status = PermissionCheck.getMicrophonePermissions();
        if (status.equals(AVAuthorizationStatus.NOT_DETERMINED)) {
            PermissionCheck.requestMicrophonePermissions();
        }

        int i = 0;
        while (status.equals(AVAuthorizationStatus.NOT_DETERMINED)) {
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

        if (status != AVAuthorizationStatus.AUTHORIZED) {
            System.err.println("Simple Voice Chat is unable to use the Microphone. Status: " + status);
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
}
