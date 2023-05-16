package GameMap.GameObjects.Structures;

import Game.Actions.Action;
import Game.Actions.DelayedAction;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import VkRender.GPUObjects.GameMapVertex;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public abstract class BuildableStructure extends Structure {

    public boolean isBuilt() {
        return isBuilt;
    }

    private boolean isBuilt = false;
    private final Duration buildTime;
    protected BuildableStructure(Duration buildTime, float maxHealth, StructureStats stats) {
        super(maxHealth, stats);
        this.buildTime = buildTime;
    }

    @Override
    public void build(GameMap gameMap, ArrayList<Action> actions, Point blockPosition) {
        this.blockPosition = blockPosition;
        actions.add(new Action((this::onBuildStart)));
        actions.add(new DelayedAction(this::onBuilt, buildTime));
    }

    protected void onBuildStart(GameMap gameMap, ArrayList<Action> actions) {
        super.onBuilt(gameMap, actions);
    }
    @Override
    protected void onBuilt(GameMap gameMap, ArrayList<Action> actions) {
        isBuilt = true;
    }
}
