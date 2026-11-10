package megalodonte.base.state;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Owns subscriptions created synchronously during a render, without owning its State. */
public final class SubscriptionScope implements AutoCloseable {
    private static final ThreadLocal<SubscriptionScope> CURRENT = new ThreadLocal<>();
    private final List<Runnable> cleanups = new ArrayList<>();
    private boolean closed;

    /** Value built by the capture and the lifetime of its subscriptions. */
    public record Captured<T>(T value, SubscriptionScope scope) {}

    private SubscriptionScope() {}

    /** Capture only work performed synchronously by builder, restoring any outer scope. */
    public static <T> Captured<T> capture(Supplier<T> builder) {
        SubscriptionScope previous = CURRENT.get();
        SubscriptionScope scope = new SubscriptionScope();
        CURRENT.set(scope);
        try {
            return new Captured<>(builder.get(), scope);
        } catch (RuntimeException | Error failure) {
            try { scope.close(); } catch (RuntimeException cleanupFailure) { failure.addSuppressed(cleanupFailure); }
            throw failure;
        } finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }

    /** Register cleanup if a render capture is active; otherwise leave ownership unchanged. */
    public static void onClose(Runnable cleanup) {
        SubscriptionScope scope = CURRENT.get();
        if (scope != null) scope.cleanups.add(cleanup);
    }

    /** Release this render's subscriptions once. Does not reset observable state values. */
    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        for (int i = cleanups.size() - 1; i >= 0; i--) {
            try { cleanups.get(i).run(); }
            catch (RuntimeException error) {
                if (failure == null) failure = error; else failure.addSuppressed(error);
            }
        }
        cleanups.clear();
        if (failure != null) throw failure;
    }
}
