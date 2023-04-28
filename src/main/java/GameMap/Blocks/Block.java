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

    public int getSize() {
        return size;
    }

    final int size;

    public Tile[][] getTiles() {
        return tiles;
    }

    Tile[][] tiles;

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    public void setStructure(@Nullable Structure structure) {
        this.structure = structure;
    }

    @Nullable
    Structure structure = null;

    public Block(int size) {
        this.size = size;
        tiles = new Tile[size][size];
    }

    Tile get(int x, int y) {
        return tiles[y][x];
    }

    public Structure getStructure() {
        return structure;
    }

}
