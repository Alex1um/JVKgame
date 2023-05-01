package GameMap.Blocks.Structures;

import Game.Actions.Action;
import Game.Actions.DelayedAction;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public abstract class BuildableStructure extends Structure {

    private final Duration buildTime;
    public final int textureIndexBuilt;
    protected BuildableStructure(int textureIndexBuilt, int textureIndexBuilding, Duration buildTime) {
        super(textureIndexBuilding);
        this.buildTime = buildTime;
        this.textureIndexBuilt = textureIndexBuilt;
    }

    @Override
    public void build(GameMap gameMap, ArrayList<Action> actions, Point position) {
        this.position = position;
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
        Block block = gameMap.getBlock(position);

        for (int tileY = 0; tileY < block.getSize(); tileY++) {
            for (int tileX = 0; tileX < block.getSize(); tileX++) {
                Tile tile = block.getTile(tileX, tileY);
                for (int vy = 0; vy < 2; vy++) {
                    for (int vx = 0; vx < 2; vx++) {
                        GameMapVertex vertex = tile.getVertex(vy, vx);
                        vertex.setTextureIndex(textureIndexBuilt);
                    }
                }
            }
        }

    }
}
