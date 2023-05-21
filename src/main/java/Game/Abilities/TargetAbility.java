package Game.Abilities;

import Game.Actions.Action;
import GameMap.GameMap;

import java.awt.Point;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class TargetAbility extends Ability {

    TriConsumer<GameMap, ArrayList<Action>, Point> onUseCallback;
    public TargetAbility(String name, TriConsumer<GameMap, ArrayList<Action>, Point> onUseCallback) {
        super(name);
        this.onUseCallback = onUseCallback;
    }

    @Override
    public void use(GameMap gameMap, ArrayList<Action> actions, Object... args) {
        Point target = (Point)args[0];
        onUseCallback.accept(gameMap, actions, target);
    }
}
