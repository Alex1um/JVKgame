package GameMap.Blocks;

import GameMap.Tiles.GrassTile;
import org.joml.Random;

public class GrassBlock extends Block {

    public GrassBlock(int blockSize, int tileSize, int blockX, int blockY) {
        super(blockSize);
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new GrassTile(tileSize, blockX * blockSize + tileX, blockY * blockSize + tileY);
            }
        }
    }

    public GrassBlock(Random r, int blockSize, int tileSize, int blockX, int blockY) {
        super(blockSize);
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new GrassTile(r, tileSize, blockX * blockSize + tileX, blockY * blockSize + tileY);
            }
        }
    }

}
