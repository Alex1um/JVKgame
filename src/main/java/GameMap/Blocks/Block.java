package GameMap.Blocks;

import GameMap.Blocks.Structures.Structure;
import GameMap.Tiles.StructureTile;
import GameMap.Tiles.Tile;
import VkRender.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Block {

    final int size;

    public Tile[][] getTiles() {
        return tiles;
    }

    Tile[][] tiles;

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    @Nullable
    Structure structure = null;

    public void destroyStructure() {
        for (int tileY = 0; tileY < size; tileY++) {
            for (int tileX = 0; tileX < size; tileX++) {
                Vector4f tileColor = new Vector4f(0f, 0f, 0f, 1f);
                Tile tile = this.tiles[tileY][tileX];
                for (int vy = 0; vy < 2; vy++) {
                    for (int vx = 0; vx < 2; vx ++) {
                        tile.getVertexColor(vy * 2 + vx, tileColor);
                        Vertex vertex = tile.getVertex(vy, vx);
                        vertex.setTextureIndex(tile.getTextureIndex());
                        vertex.setColor(tileColor);
                    }
                }
            }
        }
    }

    public void placeStructure(Structure structure) {
        if (this.structure == null) {
            this.structure = structure;
            Vector4f zero = new Vector4f(0f, 0f, 0f, 1f);
            for (int tileY = 0; tileY < size; tileY++) {
                for (int tileX = 0; tileX < size; tileX++) {
                    Tile tile = this.tiles[tileY][tileX];
                    float dx = 1f / size;
                    float dy = 1f / size;
                    for (int vy = 0; vy < 2; vy++) {
                        for (int vx = 0; vx < 2; vx++) {
                            Vertex vertex = tile.getVertex(vy, vx);
                            vertex.setTextureIndex(structure.textureIndex);
                            vertex.setColor(zero);
                            vertex.setTexCoord(new Vector2f(
                                    tileX * dx + vy * dx,
                                    tileY * dy + vx * dy
                            ));
                        }
                    }
                }
            }
        }
    }

    public Block(int size) {
        this.size = size;
        tiles = new Tile[size][size];
    }

    Tile get(int x, int y) {
        return tiles[y][x];
    }

}
