package Game.Abilities;

import Game.Actions.Action;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T, U> {
    void accept(T var1, U var2, Object... var3);

    default TriConsumer<T, U> andThen(TriConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r, t) -> {
            this.accept(l, r, t);
            after.accept(l, r, t);
        };
    }
}
