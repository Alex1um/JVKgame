package GameMap.GameObjects.Structures;

import Controller.Players.Player;
import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;

import java.awt.*;
import java.util.ArrayList;

public class Structure extends GameObject {

    public StructureStats stats;

    public Point getBlockPosition() {
        return blockPosition;
    }

    protected Point blockPosition;

    protected Structure(Player player, float maxHealth, StructureStats stats) {
        super(player, maxHealth);
        this.stats = stats;
    }

    @Override
    public void destroy(GameMap gameMap) {
        gameMap.objects.remove(this);
        owner.getStructures().remove(this);
        Block block = gameMap.getBlockByPos(blockPosition);
        block.setStructure(null);
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
