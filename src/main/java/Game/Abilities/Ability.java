package Game.Abilities;

import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;

public class Ability {
    String name;
    TriConsumer<GameMap, ArrayList<Action>> onUseCallback;

    public Ability(String name, TriConsumer<GameMap, ArrayList<Action>> onUseCallback) {
        this.name = name;
        this.onUseCallback = onUseCallback;
    }

    public void use(GameMap gameMap, ArrayList<Action> actions, Object... args) {
        this.onUseCallback.accept(gameMap, actions, args);
    }

}
