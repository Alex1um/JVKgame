package GameMap.GameObjects.Units;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.lang.annotation.*;
import java.util.ArrayList;

public class Unit extends GameObject {

    public Point tilePosition;
    final int textureIndex;

    UnitStats stats;

    protected Unit(int textureIndex, UnitStats stats) {
        super();
        this.textureIndex = textureIndex;
        this.stats = stats;
    }

    public void deploy(GameMap gameMap, ArrayList<Action> actions, Point tilePosition) {
        this.tilePosition = tilePosition;
        synchronized (actions) {
            actions.add(new Action((this::create)));
        }
    }

    private void create(GameMap gameMap, ArrayList<Action> actions) {
        Tile tile = gameMap.getTile(this.tilePosition);
        Block block = gameMap.getBlockByTilePos(this.tilePosition);
        if (block.getStructure() == null && tile.getUnit() == null) {
            tile.setUnit(this);
            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
            for (int vy = 0; vy < 2; vy++) {
                for (int vx = 0; vx < 2; vx++) {
                    GameMapVertex vertex = new GameMapVertex(
                            tile.getVertex(vx, vy).getPos(),
                            zero,
                            new Vector2f(vx, vy),
                            this.textureIndex,
                            0
                    );
                    this.setVertex(vx, vy, vertex);
                }
            }
            gameMap.objects.add(this);
        } else {
            throw new Error("Cannot place unit: Tile already occupied " + tilePosition);
        }
    }
}
