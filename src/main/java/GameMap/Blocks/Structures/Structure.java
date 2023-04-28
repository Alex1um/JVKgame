package GameMap.Blocks.Structures;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.Tiles.Tile;
import VkRender.Vertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;

public abstract class Structure {

    public final int textureIndex;

    protected Point position;

    protected Structure(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public void build(GameMap gameMap, ArrayList<Action> actions, Point position) {
        this.position = position;
        synchronized (actions) {
            actions.add(new Action((this::onBuilt)));
        }
    }

    protected void onBuilt(GameMap gameMap, ArrayList<Action> actions) {
        Block block = gameMap.getBlock(position);

        if (block.getStructure() == null) {
            block.setStructure(this);
            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
            for (int tileY = 0; tileY < block.getSize(); tileY++) {
                for (int tileX = 0; tileX < block.getSize(); tileX++) {
                    Tile tile = block.getTile(tileX, tileY);
                    float dx = 1f / block.getSize();
                    float dy = 1f / block.getSize();
                    for (int vy = 0; vy < 2; vy++) {
                        for (int vx = 0; vx < 2; vx++) {
                            Vertex vertex = tile.getVertex(vy, vx);
                            vertex.setTextureIndex(textureIndex);
                            vertex.setColor(zero);
                            vertex.setTexCoord(new Vector2f(
                                    tileX * dx + vy * dx,
                                    tileY * dy + vx * dy
                            ));
                        }
                    }
                }
            }
        } else {
            throw new Error("Cannot build structure there: " + position);
        }
    }

}
