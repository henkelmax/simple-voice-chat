package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DeviceTracker {

    private static final long DEVICE_CHECK_INTERVAL = 1000L;

    private final String deviceType;
    private final Supplier<String> defaultDeviceQuery;
    private final Supplier<List<String>> devicesQuery;
    private final Supplier<String> preferredDevice;
    @Nullable
    private String lastSeenDefaultDevice;
    private List<String> lastSeenDevices;
    private long lastDeviceCheckTime;

    public DeviceTracker(String deviceType, Supplier<String> defaultDeviceQuery, Supplier<List<String>> devicesQuery, Supplier<String> preferredDevice) {
        this.deviceType = deviceType;
        this.defaultDeviceQuery = defaultDeviceQuery;
        this.devicesQuery = devicesQuery;
        this.preferredDevice = preferredDevice;
        this.lastSeenDefaultDevice = defaultDeviceQuery.get();
        this.lastSeenDevices = devicesQuery.get();
        this.lastDeviceCheckTime = Utils.getMilliseconds();
    }

    /**
     * @param deviceOpen    if a device is currently open
     * @param currentDevice the name of the open device or <code>null</code> if it is unknown (e.g. Java mic implementation)
     * @return if the device should be reopened
     */
    public boolean shouldChangeDevice(boolean deviceOpen, @Nullable String currentDevice) {
        long time = Utils.getMilliseconds();
        if (time - lastDeviceCheckTime < DEVICE_CHECK_INTERVAL) {
            return false;
        }
        lastDeviceCheckTime = time;

        String defaultDevice = defaultDeviceQuery.get();
        List<String> devices = devicesQuery.get();
        if (Objects.equals(defaultDevice, lastSeenDefaultDevice) && devices.equals(lastSeenDevices)) {
            return false;
        }
        lastSeenDefaultDevice = defaultDevice;
        lastSeenDevices = devices;

        if (!deviceOpen) {
            Voicechat.LOGGER.info("Available {}s have changed while no {} is open", deviceType, deviceType);
            return true;
        }
        if (currentDevice == null) {
            return false;
        }
        if (!devices.contains(currentDevice)) {
            Voicechat.LOGGER.info("Current {} '{}' has disappeared", deviceType, currentDevice);
            return true;
        }
        String preferred = preferredDevice.get();
        if (preferred.isEmpty() && !Objects.equals(currentDevice, defaultDevice)) {
            Voicechat.LOGGER.info("System default {} has changed to '{}'", deviceType, defaultDevice);
            return true;
        }
        if (!preferred.isEmpty() && !preferred.equals(currentDevice) && devices.contains(preferred)) {
            Voicechat.LOGGER.info("Preferred {} '{}' has become available", deviceType, preferred);
            return true;
        }
        return false;
    }

}
