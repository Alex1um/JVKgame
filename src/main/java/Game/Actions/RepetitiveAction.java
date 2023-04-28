package Game.Actions;

import GameMap.GameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RepetitiveAction extends Action {


    public RepetitiveAction(BiConsumer<GameMap, ArrayList<Action>> callback) {
        super(callback);
    }

    @Override
    public void execute(GameMap gameMap, ArrayList<Action> actions) {
        super.execute(gameMap, actions);
        actions.add(this);
    }
}
