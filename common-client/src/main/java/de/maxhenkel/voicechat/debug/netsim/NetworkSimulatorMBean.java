package de.maxhenkel.voicechat.debug.netsim;

public interface NetworkSimulatorMBean {

    double getLossProbability();

    void setLossProbability(double lossProbability);

    double getDuplicateProbability();

    void setDuplicateProbability(double duplicateProbability);

    double getReorderProbability();

    void setReorderProbability(double reorderProbability);

    int getDelayMillis();

    void setDelayMillis(int delayMillis);

    int getJitterMillis();

    void setJitterMillis(int jitterMillis);

    void disconnect();

}
