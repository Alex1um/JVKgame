package GameMap;

import GameMap.Blocks.Block;
import GameMap.Blocks.GrassBlock;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.Config;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
    public Block getBlockByPos(int blockX, int blockY) {

        if (blockX >= 0 && blockY >= 0 && blockX < size && blockY < size ) {
            return blocks[blockY][blockX];
        } else {
            return null;
        }
    }

    @Nullable
    public Block getBlockByPos(Point blockPos) {
        return getBlockByPos(blockPos.x, blockPos.y);
    }

    @Nullable
    public Block getBlockByTilePos(int tileX, int tileY) {
        if (tileX >= 0 && tileY >= 0 && tileX < getFullTileSize() && tileY < getFullTileSize()) {
            return blocks[tileY / blockSize][tileX / blockSize];
        } else {
            return null;
        }
    }

    public Block getBlockByTilePos(Point tilePos) {
        return getBlockByTilePos(tilePos.x, tilePos.y);
    }

    public Point getBlockPosByTilePos(int tileX, int tileY) {
        return new Point(tileX / blockSize, tileY / blockSize);
    }

    public Point getBlockPosByTilePos(Point tilePos) {
        return new Point(tilePos.x / blockSize, tilePos.y / blockSize);
    }

    @Nullable
    public Tile getTile(int tileX, int tileY) {
        if (tileX >= 0 && tileY >= 0 && tileX < getFullTileSize() && tileY < getFullTileSize()) {
            int blockX = tileX / blockSize;
            int blockY = tileY / blockSize;
            int tileRelY = tileY % blockSize;
            int tileRelX = tileX % blockSize;
            return blocks[blockY][blockX].getTile(tileRelX, tileRelY);
        } else {
            return null;
        }
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
    public Point getFreeTilePos(Point center, int radius) {
        return getFreeTilePos(center, radius, 1);
    }

    public Point getFreeTilePos(Point center, int radius, int centerRadius) {
        int distance = centerRadius;
        while (distance <= radius) {
            // Перебираем клетки на расстоянии distance от центра
            for (int i = -distance; i <= distance; i++) {
                for (int j = -distance; j <= distance; j++) {
                    // Пропускаем клетки, которые находятся дальше, чем distance
                    if (Math.abs(i) + Math.abs(j) > distance) {
                        continue;
                    }
                    int x = center.x + i;
                    int y = center.y + j;
                    // Пропускаем клетки, которые находятся за пределами поля
                    if (x < 0 || y < 0 || x >= this.getFullTileSize() || y >= this.getFullTileSize()) {
                        continue;
                    }
                    // Пропускаем клетки, которые не являются пустыми
                    if (this.getTile(x, y).getUnit() != null || this.getBlockByTilePos(x, y).getStructure() != null) {
                        continue;
                    }
                    // Клетка нашлась
                    return new Point(x, y);
                }
            }
            distance++;
        }
        return null;
    }

    public boolean isTilePositionFree(Point pos) {
        return this.getBlockByTilePos(pos).getStructure() == null && this.getTile(pos).getUnit() == null;
    }
}
