package de.maxhenkel.voicechat.macos;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;

public class MacApplication {

    private final Path appPath;
    private final Path infoPlistPath;
    private final String name;
    private final NSDictionary info;

    public MacApplication(Path appPath) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {
        this.appPath = appPath;
        if (!Files.isDirectory(appPath)) {
            throw new IOException("'" + appPath + "' is not an application");
        }
        if (!appPath.toFile().getName().endsWith(".app")) {
            throw new IOException("'" + appPath + "' is not an application");
        }
        Path infoPlist = appPath.resolve("Contents").resolve("Info.plist");
        if (!Files.exists(infoPlist)) {
            throw new IOException("No Info.plist found");
        }
        infoPlistPath = infoPlist;
        info = (NSDictionary) PropertyListParser.parse(infoPlistPath.toFile());
        name = info.get("CFBundleName").toString();
    }

    public String getName() {
        return name;
    }

    public void fixMicrophoneUsageDescription() throws IOException {
        if (!info.containsKey("NSMicrophoneUsageDescription")) {
            info.put("NSMicrophoneUsageDescription", "A Minecraft mod is requesting access to the microphone");
            PropertyListParser.saveAsXML(info, infoPlistPath.toFile());
        }
    }

    public void removeSignature() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("codesign", "--verbose", "--deep", "--remove-signature", appPath.toFile().getAbsolutePath());
        Process process = builder.start();

        String stderr = captureStream(process.errorReader());
        String stdout = captureStream(process.inputReader());

        process.waitFor();

        if (process.exitValue() != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to execute codesign. Exit code ");
            sb.append(process.exitValue());
            sb.append(".");
            if (!stdout.isEmpty()) {
                sb.append("\n");
                sb.append(stdout);
            }
            if (!stderr.isEmpty()) {
                sb.append("\n");
                sb.append(stderr);
            }

            throw new IOException(sb.toString());
        }
    }

    public String captureStream(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();
    }

}
