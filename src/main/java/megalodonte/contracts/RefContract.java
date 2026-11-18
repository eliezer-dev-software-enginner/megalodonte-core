package megalodonte.contracts;

/** Platform-neutral mutable reference to a composed value. */
public interface RefContract<T> {
    T current();
    void setCurrent(T value);
}
