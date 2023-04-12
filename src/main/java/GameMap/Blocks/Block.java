package GameMap.Blocks;

import GameMap.Blocks.Structures.Structure;
import GameMap.Tiles.Tile;
import org.jetbrains.annotations.Nullable;

public class Block {

    public Tile[][] getTiles() {
        return tiles;
    }

    Tile[][] tiles;

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    @Nullable
    Structure structure = null;

    public Block(int size) {
        tiles = new Tile[size][size];
    }

    Tile get(int i, int j) {
        return tiles[j][j];
    }

}
