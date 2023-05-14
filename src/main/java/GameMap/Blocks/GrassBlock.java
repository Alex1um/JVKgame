package GameMap.Blocks;

import GameMap.Tiles.GrassTile;
import org.joml.Random;

public class GrassBlock extends Block {

    public GrassBlock(int blockSize) {
        super(blockSize);
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new GrassTile();
            }
        }
    }

}
