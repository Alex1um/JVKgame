package Game.Abilities;

import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;

public abstract class Ability {
    public String getName() {
        return name;
    }

    String name;
//    TriConsumer<GameMap, ArrayList<Action>> onUseCallback;

    public Ability(String name) {
        this.name = name;
//        this.onUseCallback = onUseCallback;
    }

    public abstract void use(GameMap gameMap, ArrayList<Action> actions, Object... args);
//        this.onUseCallback.accept(gameMap, actions, args);

}
