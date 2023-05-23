package GameMap.Blocks;

import GameMap.Tiles.GoldMineTile;
import GameMap.Tiles.GrassTile;
import GameMap.Tiles.WaterTile;
import org.joml.Random;

public class GoldMineBlock extends Block {
    public GoldMineBlock(int blockSize) {
        super(blockSize);
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new GrassTile()   ;
            }
        }
        tiles[blockSize / 2][blockSize / 2] = new GoldMineTile();
    }

    public GoldMineBlock(int blockSize, Random random) {
        super(blockSize);
        int goldMinesCount = Math.min(random.nextInt(3) + 1, blockSize); // 1 - 3
        for (int tileY = 0; tileY < blockSize; tileY++) {
            for (int tileX = 0; tileX < blockSize; tileX++) {
                tiles[tileY][tileX] = new GrassTile();
            }
        }
        while (goldMinesCount > 0) {
            int x = random.nextInt(blockSize);
            int y = random.nextInt(blockSize);
            if (tiles[y][x] instanceof GrassTile) {
                tiles[y][x] = new GoldMineTile();
                goldMinesCount--;
            }
        }
    }
}
