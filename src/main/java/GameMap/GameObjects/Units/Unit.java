package GameMap.GameObjects.Units;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;

public class Unit extends GameObject {

    public void setTilePosition(Tile newTile, Point tilePosition) {
        this.tilePosition = tilePosition;
        for (int vy = 0; vy < 2; vy++) {
            for (int vx = 0; vx < 2; vx++) {
                this.getVertex(vx, vy).setPos(newTile.getVertex(vx, vy).getPos());
            }
        }
        this.updateHealthBarPos();
    }

    public Point tilePosition;
    final int textureIndex;

    UnitStats stats;

    protected Unit(int textureIndex, float maxHealth, UnitStats stats) {
        super(maxHealth);
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
            this.updateHealthBarPos();
            this.setHealth(this.getMaxHealth());
            gameMap.objects.add(this);
        } else {
            throw new Error("Cannot place unit: Tile already occupied " + tilePosition);
        }
    }

    @Nullable
    private Action movingAction = null;
    @Nullable
    private Point movingDestination = null;
    public void move(GameMap gameMap, ArrayList<Action> actions, Point destination) {
        Action newMovingAction = new Action(this::step);
        movingDestination = destination;
        if (movingAction == null) {
            actions.add(newMovingAction);
        }
        movingAction = newMovingAction;
    }

    private void step(GameMap gameMap, ArrayList<Action> newActions) {
        if (!this.tilePosition.equals(movingDestination)) {
//            this.stats.speedTilesPerFrame
            int dx = Integer.min(this.stats.speedTilesPerFrame, Math.abs(movingDestination.x - this.tilePosition.x)) * Integer.signum(movingDestination.x - this.tilePosition.x);
            int dy = Integer.min(this.stats.speedTilesPerFrame, Math.abs(movingDestination.y - this.tilePosition.y)) * Integer.signum(movingDestination.y - this.tilePosition.y);
            Point newTilePosition = new Point(this.tilePosition.x + dx, this.tilePosition.y + dy);
            if (gameMap.isTilePosValid(newTilePosition) && gameMap.isTilePositionFree(newTilePosition)) {
                Tile newTile = gameMap.getTile(newTilePosition);
                Tile oldTile = gameMap.getTile(this.tilePosition);
                oldTile.setUnit(null);
                newTile.setUnit(this);
//                this.tilePosition = newTilePosition;
                this.setTilePosition(newTile, newTilePosition);
            }
            newActions.add(movingAction);
        } else {
            movingAction = null;
            movingDestination = null;
        }
    }
    
    
}
