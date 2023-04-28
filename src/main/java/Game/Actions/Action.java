package Game.Actions;

import GameMap.GameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Action {

    BiConsumer<GameMap, ArrayList<Action>> callback;

    public void execute(GameMap gameMap, ArrayList<Action> actions) {
        callback.accept(gameMap, actions);
    }

    public Action(BiConsumer<GameMap, ArrayList<Action>> callback) {
        this.callback = callback;
    }
}
