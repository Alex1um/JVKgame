package GameMap.GameObjects.Structures;

import Game.Actions.Action;
import GameMap.GameMap;

import java.time.Duration;
import java.util.ArrayList;

public class House extends BuildableStructure {

    public House() {
        super(Duration.ofSeconds(5), 200, new StructureStats(200, "House"));
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
