package GameMap.GameObjects.Structures;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;

public abstract class Structure extends GameObject {

    public final int textureIndex;
    public StructureStats stats;
    protected Point blockPosition;

    protected Structure(int textureIndex, float maxHealth, StructureStats stats) {
        super(maxHealth);
        this.textureIndex = textureIndex;
        this.stats = stats;
    }

    public void build(GameMap gameMap, ArrayList<Action> actions, Point blockPosition) {
        this.blockPosition = blockPosition;
        synchronized (actions) {
            actions.add(new Action((this::onBuilt)));
        }
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
            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
            for (int vy = 0; vy < 2; vy++) {
                for (int vx = 0; vx < 2; vx++) {
                    GameMapVertex vertex = new GameMapVertex(
                            block.getTile(
                                    vx * (block.getSize() - 1),
                                    vy * (block.getSize() - 1)
                            ).getVertex(vx, vy).getPos(),
                            zero,
                            new Vector2f(vx, vy),
                            this.textureIndex,
                            0
                    );
                    this.setVertex(vx, vy, vertex);
                }
            }
            this.setHealthBar();
            this.setHealth(this.getMaxHealth());
            gameMap.objects.add(this);
//            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
//            for (int tileY = 0; tileY < block.getSize(); tileY++) {
//                for (int tileX = 0; tileX < block.getSize(); tileX++) {
//                    Tile tile = block.getTile(tileX, tileY);
//                    float dx = 1f / block.getSize();
//                    float dy = 1f / block.getSize();
//                    for (int vy = 0; vy < 2; vy++) {
//                        for (int vx = 0; vx < 2; vx++) {
//                            GameMapVertex vertex = tile.getVertex(vy, vx);
//                            vertex.setTextureIndex(textureIndex);
//                            vertex.setColor(zero);
//                            vertex.setTexCoord(new Vector2f(
//                                    tileX * dx + vy * dx,
//                                    tileY * dy + vx * dy
//                            ));
//                        }
//                    }
//                }
//            }
        } else {
            throw new Error("Cannot build structure there: " + blockPosition);
        }
    }

}
