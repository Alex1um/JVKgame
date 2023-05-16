package GameMap.GameObjects.Units;

import Game.Actions.Action;
import GameMap.GameMap;
import Game.Abilities.AbilityMethod;

import java.awt.*;
import java.util.ArrayList;

public class Master extends Unit {

    public int spawnMaxRadius = 3;

    public Master() {
        super(0, 50, new UnitStats(1f));
    }

    @AbilityMethod(name = "Summon slave!")
    public void summonSlave(GameMap gameMap, ArrayList<Action> actions) throws Exception {
        Point summonPoint = gameMap.getFreeTilePos(this.getTilePosition(), this.spawnMaxRadius);
        if (summonPoint != null) {
            new Slave().deploy(gameMap, actions, summonPoint);
        } else {
            throw new Exception("cannot summon unit: no empty space");
        }
    }
}
