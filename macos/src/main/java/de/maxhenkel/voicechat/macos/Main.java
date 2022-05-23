package de.maxhenkel.voicechat.macos;

import com.sun.jna.Platform;
import de.maxhenkel.voicechat.macos.jna.avfoundation.AVAuthorizationStatus;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class Main {

    public static final String VERSION;

    static {
        String version = "N/A";
        try {
            version = readVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
        VERSION = version;
    }

    public static void main(String[] args) {
        System.out.printf("MacOS patcher version %s%n", VERSION);

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

    private static String readVersion() throws IOException {
        Enumeration<URL> resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            String version = manifest.getMainAttributes().getValue("Patcher-Version");
            if (version != null) {
                return version;
            }
        }
        // Use the environment variable in development
        String env = System.getenv("PATCHER_VERSION");
        if (env != null) {
            return env;
        }
        throw new IOException("Could not read MANIFEST.MF");
    }

}
