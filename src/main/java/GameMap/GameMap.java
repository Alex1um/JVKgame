package GameMap;

import GameMap.Blocks.Block;
import GameMap.Blocks.GrassBlock;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.Config;
import org.joml.Random;

import java.awt.*;
import java.util.ArrayList;

public class GameMap {

    Block[][] blocks;
    int size;
    int blockSize;
    public int getSize() {
        return size;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public Block getBlock(int blockX, int blockY) {
        return blocks[blockY][blockX];
    }

    public Block getBlock(Point pos) {
        return blocks[pos.y][pos.x];
    }

    public Block getBlockByTilePos(int tileX, int tileY) {
        return blocks[tileY / blockSize][tileX / blockSize];
    }

    public Point getBlockPosByTilePos(int tileX, int tileY) {
        return new Point(tileX / blockSize, tileY / blockSize);
    }

    public Tile getTile(int tileX, int tileY) {
        int blockX = tileX / blockSize;
        int blockY = tileY / blockSize;
        int tileRelY = tileY % blockSize;
        int tileRelX = tileX % blockSize;

        return blocks[blockY][blockX].getTile(tileRelX, tileRelY);
    }

    public Tile getTile(Point tilePosition) {
        return getTile(tilePosition.x, tilePosition. y);
    }

    public int getFullTileSize() {
        return size * blockSize;
    }

    public ArrayList<GameObject> objects = new ArrayList<>();
    public GameMap(int size, int blockSize) {
        this.blockSize = blockSize;
        this.size = size;
        blocks = new Block[size][size];
    }

    public void generateRandomMap(Long seed) {
        Random r = new Random(seed);
        for (int blockY = 0; blockY < size; blockY++) {
            for (int blockX = 0; blockX < size; blockX++) {
                blocks[blockY][blockX] = new GrassBlock(r, blockSize, Config.tileSize, blockX, blockY);
            }
        }
    }

}
