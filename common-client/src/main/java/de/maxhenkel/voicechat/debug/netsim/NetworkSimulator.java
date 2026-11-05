package de.maxhenkel.voicechat.debug.netsim;

import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Used to simulate poor network conditions.
 * Change values by running <code>jconsole</code>.
 */
public class NetworkSimulator implements NetworkSimulatorMBean {

    public static final NetworkSimulator INSTANCE = new NetworkSimulator();

    private volatile double lossProbability;
    private volatile double duplicateProbability;
    private volatile double reorderProbability;
    private volatile int delayMillis;
    private volatile int jitterMillis;
    @Nullable
    private volatile SimulatedClientVoicechatSocket socket;

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(INSTANCE, new ObjectName("de.maxhenkel.voicechat:type=NetworkSimulator"));
            Voicechat.LOGGER.info("Registered network simulator MBean");
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to register network simulator MBean", e);
        }
    }

    public void setSocket(SimulatedClientVoicechatSocket socket) {
        this.socket = socket;
    }

    @Override
    public double getLossProbability() {
        return lossProbability;
    }

    @Override
    public void setLossProbability(double lossProbability) {
        this.lossProbability = lossProbability;
    }

    @Override
    public double getDuplicateProbability() {
        return duplicateProbability;
    }

    @Override
    public void setDuplicateProbability(double duplicateProbability) {
        this.duplicateProbability = duplicateProbability;
    }

    @Override
    public double getReorderProbability() {
        return reorderProbability;
    }

    @Override
    public void setReorderProbability(double reorderProbability) {
        this.reorderProbability = reorderProbability;
    }

    @Override
    public int getDelayMillis() {
        return delayMillis;
    }

    @Override
    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public int getJitterMillis() {
        return jitterMillis;
    }

    @Override
    public void setJitterMillis(int jitterMillis) {
        if (jitterMillis < 0) {
            throw new IllegalArgumentException("Jitter can't be negative");
        }
        this.jitterMillis = jitterMillis;
    }

    @Override
    public void disconnect() {
        SimulatedClientVoicechatSocket s = socket;
        if (s != null) {
            s.simulateFailure();
        }
    }

}
