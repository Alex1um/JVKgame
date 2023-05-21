package Game.Abilities;

import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class BasicAbility extends Ability {

    BiConsumer<GameMap, ArrayList<Action>> onUseCallback;

    public BasicAbility(String name, BiConsumer<GameMap, ArrayList<Action>> onUseCallback) {
        super(name);
        this.onUseCallback = onUseCallback;
    }

    @Override
    public void use(GameMap gameMap, ArrayList<Action> actions, Object ...args) {
        this.onUseCallback.accept(gameMap, actions);
    }

}
