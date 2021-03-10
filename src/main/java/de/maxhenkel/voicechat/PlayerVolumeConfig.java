package de.maxhenkel.voicechat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class PlayerVolumeConfig {

    private Properties properties;
    private Path path;

    public PlayerVolumeConfig() {
        path = FMLPaths.CONFIGDIR.get().resolve(Main.MODID).resolve("player-volumes.properties");
        properties = new Properties();
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            properties.load(new FileInputStream(file));
        }
    }

    public void save() throws IOException {
        File file = path.toFile();
        file.getParentFile().mkdirs();
        properties.store(new FileWriter(file, false), "The adjusted volumes for all other players");
    }

    public double getVolume(UUID uuid, double def) {
        String property = properties.getProperty(uuid.toString());
        if (property == null) {
            return setVolume(uuid, def);
        }
        try {
            return Double.parseDouble(property);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return setVolume(uuid, def);
        }
    }

    public double getVolume(PlayerEntity playerEntity) {
        return getVolume(playerEntity.getUUID(), 1D);
    }

    public double setVolume(UUID uuid, double value) {
        properties.put(uuid.toString(), String.valueOf(value));
        new Thread(() -> {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return value;
    }
}
