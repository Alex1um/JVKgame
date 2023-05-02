package GameMap.GameObjects.Units;

import Game.Actions.Action;
import GameMap.GameMap;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;

public class Unit {

    public Point tilePosition;
    int textureIndex;

    UnitStats stats;

    protected Unit(int textureIndex, UnitStats stats) {
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
        if (tile.getUnit() == null) {
            tile.setUnit(this);
            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
            for (int vy = 0; vy < 2; vy++) {
                for (int vx = 0; vx < 2; vx++) {
                    GameMapVertex vertex = tile.getVertex(vy, vx);
                    vertex.setTextureIndex(textureIndex);
                    vertex.setColor(zero);
                }
            }
        } else {
            throw new Error("Cannot place unit: Tile already occupied " + tilePosition);
        }
    }
}
