package Game.Abilities;

import Game.Actions.Action;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T var1, U var2, V var3);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (l, r, t) -> {
            this.accept(l, r, t);
            after.accept(l, r, t);
        };
    }
}
