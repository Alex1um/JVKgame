package GameMap.GameObjects.Units;

import Controller.Players.Player;
import Game.Abilities.BasicAbilityMethod;
import Game.Actions.Action;
import GameMap.GameMap;
import Game.Abilities.AbilityMethod;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public class Necromancer extends Unit {

    public int spawnMaxRadius = 3;

    public Necromancer(Player player) {
        super(player, 500, new UnitStats(.1f, 5, 1, Duration.ofMillis(1500), 1));
    }

    @BasicAbilityMethod(name = "Summon zombie!")
    public void summonSlave(GameMap gameMap, ArrayList<Action> actions) throws Exception {
        Point summonPoint = gameMap.getFreeTilePos(this.getTilePosition(), this.spawnMaxRadius);
        if (summonPoint != null) {
            new Zombie(owner).deploy(gameMap, actions, summonPoint);
        } else {
            throw new Exception("cannot summon unit: no empty space");
        }
    }
}
