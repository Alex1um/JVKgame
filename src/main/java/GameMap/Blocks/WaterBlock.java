package GameMap.Blocks;

import GameMap.Tiles.GrassTile;
import GameMap.Tiles.WaterTile;

public class WaterBlock extends Block {
    public WaterBlock(int blockSize) {
        super(blockSize);
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new WaterTile();
            }
        }
    }
}
