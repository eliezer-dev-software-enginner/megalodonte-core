package megalodonte.contracts;

/** Screen composition and lifecycle without native toolkit types. */
public interface ScreenComponentContract<C> extends ScreenLifecycle {
    C render();
}
