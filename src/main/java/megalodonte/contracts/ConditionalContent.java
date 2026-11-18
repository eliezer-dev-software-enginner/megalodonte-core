package megalodonte.contracts;

import java.util.List;
import java.util.Optional;
import megalodonte.base.state.ReadableState;

/** Platform-neutral contract for a conditional composition block. */
public interface ConditionalContent<C> {
    ReadableState<Boolean> condition();
    C trueContent();
    Optional<C> falseContent();

    default List<C> activeContent() {
        if (Boolean.TRUE.equals(condition().get())) return List.of(trueContent());
        return falseContent().map(List::of).orElseGet(List::of);
    }
}
