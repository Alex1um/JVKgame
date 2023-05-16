package GameMap.GameObjects.Structures;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;

import java.awt.*;
import java.util.ArrayList;

public abstract class Structure extends GameObject {

    public StructureStats stats;

    public Point getBlockPosition() {
        return blockPosition;
    }

    protected Point blockPosition;

    protected Structure(float maxHealth, StructureStats stats) {
        super(maxHealth);
        this.stats = stats;
    }

    public void build(GameMap gameMap, ArrayList<Action> actions, Point blockPosition) {
        this.blockPosition = blockPosition;
        actions.add(new Action((this::onBuilt)));
    }

    protected void onBuilt(GameMap gameMap, ArrayList<Action> actions) {
        Block block = gameMap.getBlockByPos(blockPosition);

        boolean canBuild = block.getStructure() == null;
        if (canBuild) {
            searchLoop:
            for (Tile[] TileRow : block.getTiles()) {
                for (Tile tile : TileRow) {
                    if (tile.getUnit() != null) {
                        canBuild = false;
                        break searchLoop;
                    }
                }
            }
        }

        if (canBuild) {
            block.setStructure(this);
            this.setHealth(this.getMaxHealth());
            gameMap.objects.add(this);
        } else {
            throw new Error("Cannot build structure there: " + blockPosition);
        }
    }

}
