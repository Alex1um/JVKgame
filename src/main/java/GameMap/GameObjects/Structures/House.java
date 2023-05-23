package GameMap.GameObjects.Structures;

import Controller.Players.Player;
import Game.Actions.Action;
import GameMap.GameMap;

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
}
