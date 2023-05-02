package GameMap.Blocks;

import GameMap.GameObjects.Structures.Structure;
import GameMap.Tiles.Tile;
import org.jetbrains.annotations.Nullable;

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
