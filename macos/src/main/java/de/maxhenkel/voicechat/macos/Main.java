package de.maxhenkel.voicechat.macos;

import com.sun.jna.Platform;

import java.awt.GraphicsEnvironment;

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
