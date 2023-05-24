package GameMap.GameObjects.Structures;

import Controller.Players.Player;
import Game.Abilities.BasicAbilityMethod;
import Game.Actions.Action;
import GameMap.GameMap;
import GameMap.GameObjects.Units.Necromancer;
import GameMap.GameObjects.Units.Worker;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public class House extends BuildableStructure {

    public House(Player player) {
        super(player, Duration.ofSeconds(5), 200, new StructureStats(200, "House"));
    }

    @Override
    protected void onBuildStart(GameMap gameMap, ArrayList<Action> actions) {
        super.onBuildStart(gameMap, actions);
    }

    @Override
    protected void onBuilt(GameMap gameMap, ArrayList<Action> actions) {
        super.onBuilt(gameMap, actions);
    }


    @BasicAbilityMethod(name = "Summon Necromancer")
    public void summonNecromancer(GameMap gameMap, ArrayList<Action> actions) throws Exception {
        if (owner.getMoney() < 100) {
            throw new Exception("Not enough money");
        }
        Point center = new Point(
                blockPosition.x * gameMap.getBlockSizeTiles() + gameMap.getBlockSizeTiles() / 2,
                blockPosition.y * gameMap.getBlockSizeTiles() + gameMap.getBlockSizeTiles() / 2
        );
        Point summonPoint = gameMap.getFreeTilePos(center, gameMap.getBlockSizeTiles(), gameMap.getBlockSizeTiles());
        if (summonPoint != null) {
            new Necromancer(owner).deploy(gameMap, actions, summonPoint);
        } else {
            throw new Exception("cannot summon unit: no empty space");
        }
    }
}
