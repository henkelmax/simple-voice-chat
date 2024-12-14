package de.maxhenkel.voicechat.voice.client;

public class ClientUtils {

    /**
     * Gets the default voice chat distance
     *
     * @return 48 if the voice chat is not connected
     */
    public static float getDefaultDistanceClient() {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return 48F;
        }
        ClientVoicechatConnection connection = client.getConnection();
        if (connection == null) {
            return 48F;
        }
        return (float) connection.getData().getVoiceChatDistance();
    }

}
