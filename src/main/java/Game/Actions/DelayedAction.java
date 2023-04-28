package Game.Actions;

import GameMap.GameMap;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class DelayedAction extends Action {

    public Instant getStartTime() {
        return startTime;
    }

    private Instant startTime;

    public Duration getDelay() {
        return delay;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    private Duration delay;

    public DelayedAction(BiConsumer<GameMap, ArrayList<Action>> callback, Duration delay) {
        super(callback);
        this.delay = delay;
        startTime = Instant.now();
    }

    @Override
    public void execute(GameMap gameMap, ArrayList<Action> actions) {
        if (Duration.between(startTime, Instant.now()).compareTo(delay) >= 0) {
            callback.accept(gameMap, actions);
        } else {
            actions.add(this);
        }
    }
}
