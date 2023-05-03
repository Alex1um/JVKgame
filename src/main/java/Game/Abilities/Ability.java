package Game.Abilities;

import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;

public class Ability {
    String name;
    Action actionOnUse;

    public Ability(String name, Action actionOnUse) {
        this.name = name;
        this.actionOnUse = actionOnUse;
    }

    public void use(ArrayList<Action> actions) {
        actions.add(actionOnUse);
    }

}
