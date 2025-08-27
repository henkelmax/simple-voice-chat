package de.maxhenkel.voicechat.natives;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class NativeUtils {

    @Nullable
    public static <T> T createSafe(SafeSupplier<T> supplier, @Nullable Consumer<Throwable> onError, long waitTime) {
        AtomicReference<Throwable> exception = new AtomicReference<>();
        AtomicReference<T> obj = new AtomicReference<>();
        Thread t = new Thread(() -> {
            if (onError != null) {
                Thread.setDefaultUncaughtExceptionHandler((t1, e) -> {
                    exception.set(e);
                });
            }
            try {
                obj.set(supplier.get());
            } catch (Throwable e) {
                exception.set(e);
            }
        }, "NativeInitializationThread");
        t.start();

        try {
            t.join(waitTime);
        } catch (InterruptedException e) {
            return null;
        }
        Throwable ex = exception.get();
        if (onError != null && ex != null) {
            onError.accept(ex);
        }
        return obj.get();
    }

    @Nullable
    public static <T> T createSafe(SafeSupplier<T> supplier, @Nullable Consumer<Throwable> onError) {
        return createSafe(supplier, onError, 5000);
    }

    @Nullable
    public static <T> T createSafe(SafeSupplier<T> supplier) {
        return createSafe(supplier, null);
    }

    @FunctionalInterface
    public interface SafeSupplier<T> {
        T get() throws Throwable;
    }
}
