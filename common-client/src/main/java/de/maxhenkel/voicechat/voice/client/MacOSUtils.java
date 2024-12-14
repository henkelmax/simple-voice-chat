package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacOSUtils {

    public static void checkPermissionInSeparateProcess() {
        try {
            Voicechat.LOGGER.info("Checking for microphone permission - This may take up to 5 seconds");
            Path path = copyJar();
            int exitCode = execInProcess(path, "de.maxhenkel.voicechat.macos.Main");
            Voicechat.LOGGER.info("Running permission check process ({})", exitCode);
            if (exitCode != 0) {
                exitCode = execInProcess(path, "de.maxhenkel.voicechat.macos.Main", "gui");
                Voicechat.LOGGER.info("Running patch GUI process ({})", exitCode);
                if (exitCode == 0) {
                    Voicechat.LOGGER.error("Don't forget to restart your game!");
                }
            }
        } catch (Exception e) {
            Voicechat.LOGGER.info("Failed permission check: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Nullable
    private static String getJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            return null;
        }
        return javaHome + File.separator + "bin" + File.separator + "java";
    }

    private static Path copyJar() throws IOException {
        URL macJar = MacOSUtils.class.getClassLoader().getResource("macos.zip");
        if (macJar == null) {
            throw new IOException("Resource not found");
        }
        Path tempDir = Files.createTempDirectory("voicechat");

        Path macJarPath = tempDir.resolve("macos.jar");
        macJarPath.toFile().deleteOnExit();
        tempDir.toFile().deleteOnExit();
        FileUtils.copyURLToFile(macJar, macJarPath.toFile());
        return macJarPath;
    }

    private static int execInProcess(Path jarFile, String className, String... args) throws IOException, InterruptedException {
        String javaExecutable = getJavaExecutable();

        if (javaExecutable == null) {
            throw new IOException("Couldn't find Java executable");
        }

        String classpath = System.getProperty("java.class.path");

        if (classpath == null) {
            classpath = jarFile.toFile().getAbsolutePath();
        } else {
            classpath = classpath + ":" + jarFile.toFile().getAbsolutePath();
        }

        List<String> command = new ArrayList<>();
        command.add(javaExecutable);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.inheritIO().start();
        process.waitFor();
        return process.exitValue();
    }

}
