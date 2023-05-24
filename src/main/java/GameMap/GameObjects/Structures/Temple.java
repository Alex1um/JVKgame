package GameMap.GameObjects.Structures;


import Controller.Players.Player;
import Game.Abilities.BasicAbilityMethod;
import Game.Actions.Action;
import GameMap.GameMap;
import GameMap.GameObjects.Units.Necromancer;
import GameMap.GameObjects.Units.Worker;
import GameMap.GameObjects.Units.Zombie;

import java.awt.*;
import java.util.ArrayList;

public class Temple extends Structure {

    public Temple(Player player) {
        super(player, 500, new StructureStats(500, "Temple"));
    }


    @BasicAbilityMethod(name = "Summon worker")
    public void summonWorker(GameMap gameMap, ArrayList<Action> actions) throws Exception {

        Point center = new Point(
                blockPosition.x * gameMap.getBlockSizeTiles() + gameMap.getBlockSizeTiles() / 2,
                blockPosition.y * gameMap.getBlockSizeTiles() + gameMap.getBlockSizeTiles() / 2
                );
        Point summonPoint = gameMap.getFreeTilePos(center, gameMap.getBlockSizeTiles(), gameMap.getBlockSizeTiles());
        if (summonPoint != null) {
            owner.takeMoney(50);
            new Worker(owner).deploy(gameMap, actions, summonPoint);
        } else {
            throw new Exception("cannot summon unit: no empty space");
        }
    }

}
