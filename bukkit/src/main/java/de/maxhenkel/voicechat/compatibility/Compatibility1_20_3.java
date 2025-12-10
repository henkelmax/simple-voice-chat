package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_20_3 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_20_3 = BukkitVersion.parseBukkitVersion("1.20.3-R0.1");
    public static final BukkitVersion VERSION_1_20_4 = BukkitVersion.parseBukkitVersion("1.20.4-R0.1");
    public static final BukkitVersion VERSION_1_20_5 = BukkitVersion.parseBukkitVersion("1.20.5-R0.1");
    public static final BukkitVersion VERSION_1_20_6 = BukkitVersion.parseBukkitVersion("1.20.6-R0.1");
    public static final BukkitVersion VERSION_1_21 = BukkitVersion.parseBukkitVersion("1.21-R0.1");
    public static final BukkitVersion VERSION_1_21_1 = BukkitVersion.parseBukkitVersion("1.21.1-R0.1");
    public static final BukkitVersion VERSION_1_21_2 = BukkitVersion.parseBukkitVersion("1.21.2-R0.1");
    public static final BukkitVersion VERSION_1_21_3 = BukkitVersion.parseBukkitVersion("1.21.3-R0.1");
    public static final BukkitVersion VERSION_1_21_4 = BukkitVersion.parseBukkitVersion("1.21.4-R0.1");

    public static final Compatibility1_20_3 INSTANCE = new Compatibility1_20_3();

    private Method playerArgument;
    private Method uuidArgument;
    private Method fromJson;
    private Method getHandle;
    private Method sendSystemMessage;

    public void init() throws Exception {
        super.init();

        Class<?> argumentEntityClass = getClazz(
                "net.minecraft.commands.arguments.ArgumentEntity",
                "net.minecraft.commands.arguments.EntityArgument"
        );
        playerArgument = getMethod(argumentEntityClass, "c", "entity");

        Class<?> argumentUuidClass = getClazz(
                "net.minecraft.commands.arguments.ArgumentUUID",
                "net.minecraft.commands.arguments.UuidArgument"
        );
        uuidArgument = getMethod(argumentUuidClass, "a", "uuid");

        Class<?> craftPlayer = getBukkitClass("entity.CraftPlayer");
        getHandle = getMethod(craftPlayer, "getHandle");
        Class<?> componentClass = getClazz(
                "net.minecraft.network.chat.IChatBaseComponent",
                "net.minecraft.network.chat.Component"
        );
        Class<?> craftChatMessageClass = getBukkitClass("util.CraftChatMessage");
        fromJson = getMethod(craftChatMessageClass, new String[]{"fromJSON"}, new Class[]{String.class});
        Class<?> player = getClazz("net.minecraft.server.level.EntityPlayer", "net.minecraft.server.level.ServerPlayer");
        sendSystemMessage = getMethod(player, new String[]{"a", "sendSystemMessage"}, new Class[]{componentClass, boolean.class});
    }

    @Override
    public void sendJsonMessage(Player player, String json) {
        send(player, json, false);
    }

    @Override
    public void sendJsonStatusMessage(Player player, String json) {
        send(player, json, true);
    }

    @Override
    public String createTranslationMessage(String key, String... args) {
        return Compatibility1_8.constructTranslationMessage(key, args);
    }

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendJsonMessage(player, Compatibility1_8.constructInviteMessage(commandSender, groupName, joinCommand));
    }

    @Override
    public void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName) {
        sendJsonMessage(player, Compatibility1_8.constructIncompatibleMessage(pluginVersion, pluginName));
    }

    @Override
    public ArgumentType<?> playerArgument() {
        return call(playerArgument, null);
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return call(uuidArgument, null);
    }

    protected void send(Player player, String json, boolean status) {
        call(sendSystemMessage, call(getHandle, player), call(fromJson, null, json), status);
    }

}
