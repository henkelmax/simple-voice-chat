package de.maxhenkel.voicechat.natives;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Cleaner {

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private final ConcurrentMap<CleanableRef, Boolean> registry = new ConcurrentHashMap<>();
    private final Thread cleanerThread;

    public static Cleaner create() {
        return new Cleaner();
    }

    private Cleaner() {
        this.cleanerThread = new Thread(() -> {
            while (true) {
                try {
                    CleanableRef ref = (CleanableRef) queue.remove();
                    ref.clean();
                } catch (InterruptedException ignored) {
                }
            }
        }, "CleanerThread");
        this.cleanerThread.setDaemon(true);
        this.cleanerThread.start();
    }

    public Cleanable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj, "obj");
        Objects.requireNonNull(action, "action");
        CleanableRef ref = new CleanableRef(this, obj, action);
        registry.put(ref, Boolean.TRUE);
        return ref;
    }

    private static final class CleanableRef extends PhantomReference<Object> implements Cleanable {
        private final Cleaner owner;
        private final Runnable action;
        private final AtomicBoolean cleaned = new AtomicBoolean(false);

        CleanableRef(Cleaner owner, Object obj, Runnable action) {
            super(obj, owner.queue);
            this.owner = owner;
            this.action = action;
        }

        @Override
        public void clean() {
            if (cleaned.compareAndSet(false, true)) {
                owner.registry.remove(this);
                try {
                    action.run();
                } catch (Throwable ignored) {
                } finally {
                    clear();
                }
            }
        }
    }

    public interface Cleanable {
        void clean();
    }

}
