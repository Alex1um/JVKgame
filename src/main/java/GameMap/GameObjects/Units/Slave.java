package GameMap.GameObjects.Units;

import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import GameMap.GameMap;

import java.util.ArrayList;

public class Slave extends Unit {
    public Slave() {
        super(1, 5, new UnitStats(5, 1));
    }
}
