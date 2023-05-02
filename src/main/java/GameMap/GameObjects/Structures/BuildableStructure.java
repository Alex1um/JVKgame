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

    private final Duration buildTime;
    public final int textureIndexBuilt;
    protected BuildableStructure(int textureIndexBuilt, int textureIndexBuilding, Duration buildTime, StructureStats stats) {
        super(textureIndexBuilding, stats);
        this.buildTime = buildTime;
        this.textureIndexBuilt = textureIndexBuilt;
    }

    @Override
    public void build(GameMap gameMap, ArrayList<Action> actions, Point blockPosition) {
        this.blockPosition = blockPosition;
        synchronized (actions) {
            actions.add(new Action((this::onBuildStart)));
            actions.add(new DelayedAction(this::onBuilt, buildTime));
        }
    }

    protected void onBuildStart(GameMap gameMap, ArrayList<Action> actions) {
        super.onBuilt(gameMap, actions);
    }

    @Override
    protected void onBuilt(GameMap gameMap, ArrayList<Action> actions) {

        for (int vy = 0; vy < 2; vy++) {
            for (int vx = 0; vx < 2; vx++) {
                GameMapVertex vertex = this.getVertex(vx, vy);
                vertex.setTextureIndex(this.textureIndexBuilt);
            }
        }

    }
}
