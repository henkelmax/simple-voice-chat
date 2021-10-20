package de.maxhenkel.voicechat.macos;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
        ProcessBuilder builder = new ProcessBuilder("codesign", "--deep", "--remove-signature", appPath.toFile().getAbsolutePath());
        Process process = builder.inheritIO().start();
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException("Exit code " + process.exitValue());
        }
    }

}
