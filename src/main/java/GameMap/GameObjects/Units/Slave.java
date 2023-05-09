package GameMap.GameObjects.Units;

import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;

public class Slave extends Unit {
    public Slave() {
        super(1, 5, new UnitStats(5, 1));
    }

    @AbilityMethod(name = "test")
    public void testMethod(GameMap gameMap, ArrayList<Action> actions, int val1, int val2) {
        System.out.println(String.format("Testing method! %d %d", val1, val2));
    }
}
