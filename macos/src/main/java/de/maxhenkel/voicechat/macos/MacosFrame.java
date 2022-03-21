package de.maxhenkel.voicechat.macos;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class MacosFrame extends JFrame implements DropTargetListener {

    private final JLabel dragText;

    public MacosFrame() {
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.application.name", "Simple Voice Chat");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 500));
        setSize(900, 700);
        setTitle("Simple Voice Chat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Container pane = getContentPane();

        JEditorPane ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setText("""
                <body style="font-family: Sans-Serif;">
                    <h1 style="text-align: center;">Simple Voice Chat MacOS Microphone Permission</h1>
                    <p>
                    Your Minecraft launcher does not allow access to your microphone.
                    </p>
                    <p><b>Please read <a href="https://github.com/henkelmax/simple-voice-chat/tree/1.18.1/macos#how-to-patch-your-launcher">this guide</a> on how to patch your launcher.</b></p>
                    <p>
                    You need to drag your Minecraft launcher from your applications folder onto the area below.
                    Note that this will remove the original signature of your launcher.
                    If you don't trust this, you can take a look at the <a href="https://github.com/henkelmax/simple-voice-chat/tree/1.18.1/macos">source code</a> of this application.
                    </p>
                    <p>
                    Doing this may result in your launcher not working correctly.
                    </p>
                    <p><b>Use at your own risk!</b></p>
                    <p align="right">Version %s</p>
                </body>
                """.formatted(Main.VERSION));
        ep.setEditable(false);
        ep.setBorder(new LineBorder(Color.WHITE, 10));
        ep.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        pane.add(ep, BorderLayout.PAGE_START);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new LineBorder(Color.WHITE, 10));
        JPanel dragPanel = new JPanel();
        dragPanel.setBorder(BorderFactory.createDashedBorder(Color.BLACK, 3, 5, 5, true));
        dragPanel.setBackground(Color.WHITE);
        dragPanel.setLayout(new BorderLayout());

        dragText = new JLabel("Drag the launcher application here");
        dragText.setFont(new Font(null, Font.PLAIN, 24));
        dragText.setHorizontalAlignment(JTextField.CENTER);

        dragText.setBorder(null);
        dragPanel.add(dragText, BorderLayout.CENTER);

        panel.add(dragPanel, BorderLayout.CENTER);
        pane.add(panel, BorderLayout.CENTER);

        setVisible(true);

        new DropTarget(this, this);
    }

    public void setApplication(MacApplication application) {
        int result = JOptionPane.showOptionDialog(null, """
                              Do you really want to patch '%s'?
                              Use at your own risk!
                        """
                        .formatted(application.getName()),
                "Patch Application",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Yes", "No", "Cancel"}, null);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            application.removeSignature();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            application.fixMicrophoneUsageDescription();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, """
                Successfully patched '%s'.
                Please restart your game and launcher for this change to take effect.
                """.formatted(application.getName()), "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetDragText() {
        dragText.setText("Drag the launcher application here");
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        dragText.setText("");
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        resetDragText();
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable tr = dtde.getTransferable();
        DataFlavor[] flavors = tr.getTransferDataFlavors();

        for (DataFlavor flavor : flavors) {
            try {
                if (flavor.isFlavorJavaFileListType()) {
                    dtde.acceptDrop(dtde.getDropAction());
                    List<File> transferData = (List<File>) tr.getTransferData(flavor);
                    for (File file : transferData) {
                        resetDragText();
                        try {
                            MacApplication application = new MacApplication(file.toPath());
                            setApplication(application);
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
