package de.maxhenkel.voicechat.macos;

import com.sun.jna.Platform;

import javax.swing.*;
import java.awt.*;

public class StandaloneMain {

    public static void main(String[] args) {
        System.out.printf("MacOS patcher version %s%n", Main.VERSION);

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("This application does not work in headless mode");
            System.exit(-1);
        }
        if (!Platform.isMac()) {
            JOptionPane.showMessageDialog(null, "You are not on MacOS", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Main.showGui();
    }

}
