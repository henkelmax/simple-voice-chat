package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import javax.annotation.Nullable;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Uses the same way Minecraft looks up SRV records, just with <code>_voicechat._udp</code> instead of <code>_minecraft._tcp</code>.
 *
 * @see net.minecraft.client.multiplayer.resolver.ServerRedirectHandler
 */
public class SrvLookup {

    @Nullable
    private static final DirContext CONTEXT = createContext();

    @Nullable
    private static DirContext createContext() {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            env.put("com.sun.jndi.dns.timeout.retries", "1");
            return new InitialDirContext(env);
        } catch (Throwable e) {
            Voicechat.LOGGER.error("Failed to initialize SRV lookup", e);
            return null;
        }
    }

    @Nullable
    public static ServerAddress lookup(String host) {
        if (CONTEXT == null) {
            return null;
        }
        try {
            Attribute srv = CONTEXT.getAttributes(String.format("_voicechat._udp.%s", host), new String[]{"SRV"}).get("srv");
            if (srv == null) {
                return null;
            }
            //Format: priority weight port target
            String[] parts = srv.get().toString().split(" ", 4);
            return new ServerAddress(parts[3], Integer.parseInt(parts[2]));
        } catch (NameNotFoundException ignored) {
            return null;
        } catch (NamingException e) {
            Voicechat.LOGGER.warn("Failed to look up voicechat SRV record: {}", e.toString());
            return null;
        } catch (Throwable e) {
            Voicechat.LOGGER.warn("Invalid voicechat SRV record for '{}': {}", host, e.toString());
            return null;
        }
    }

}
