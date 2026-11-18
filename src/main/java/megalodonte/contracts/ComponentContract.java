package megalodonte.contracts;

/** Common contract implemented by platform components. */
public interface ComponentContract<C> {
    /** Binds this component to a platform-neutral reference and returns itself. */
    <T extends C> C ref(RefContract<T> reference);
}
