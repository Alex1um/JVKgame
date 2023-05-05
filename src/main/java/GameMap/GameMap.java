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
    int mapSizeBlocks;
    int blockSizeTiles;
    public int getMapSizeBlocks() {
        return mapSizeBlocks;
    }

    public int getBlockSizeTiles() {
        return blockSizeTiles;
    }

    @Nullable
    public Block getBlockByPos(int blockX, int blockY) {

        if (blockX >= 0 && blockY >= 0 && blockX < mapSizeBlocks && blockY < mapSizeBlocks) {
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
            return blocks[tileY / blockSizeTiles][tileX / blockSizeTiles];
        } else {
            return null;
        }
    }
    public boolean isTilePosValid(int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileX < getFullTileSize() && tileY < getFullTileSize();
    }

    public boolean isTilePosValid(Point tilePos) {
        return isTilePosValid(tilePos.x, tilePos.y);
    }

    public boolean isBlockPosValid(int blockX, int blockY) {
        return blockX >= 0 && blockY >= 0 && blockX < getMapSizeBlocks() && blockY < getMapSizeBlocks();
    }
    public boolean isBlockPosValid(Point blockPos) {
        return isBlockPosValid(blockPos.x, blockPos.y);
    }

    public Block getBlockByTilePos(Point tilePos) {
        return getBlockByTilePos(tilePos.x, tilePos.y);
    }

    public Point getBlockPosByTilePos(int tileX, int tileY) {
        return new Point(tileX / blockSizeTiles, tileY / blockSizeTiles);
    }

    public Point getBlockPosByTilePos(Point tilePos) {
        return new Point(tilePos.x / blockSizeTiles, tilePos.y / blockSizeTiles);
    }

    @Nullable
    public Tile getTile(int tileX, int tileY) {
        if (tileX >= 0 && tileY >= 0 && tileX < getFullTileSize() && tileY < getFullTileSize()) {
            int blockX = tileX / blockSizeTiles;
            int blockY = tileY / blockSizeTiles;
            int tileRelY = tileY % blockSizeTiles;
            int tileRelX = tileX % blockSizeTiles;
            return blocks[blockY][blockX].getTile(tileRelX, tileRelY);
        } else {
            return null;
        }
    }

    public Tile getTile(Point tilePosition) {
        return getTile(tilePosition.x, tilePosition. y);
    }

    public int getFullTileSize() {
        return mapSizeBlocks * blockSizeTiles;
    }

    public ArrayList<GameObject> objects = new ArrayList<>();
    public GameMap(int mapSizeBlocks, int blockSizeTiles) {
        this.blockSizeTiles = blockSizeTiles;
        this.mapSizeBlocks = mapSizeBlocks;
        blocks = new Block[mapSizeBlocks][mapSizeBlocks];
    }

    public void generateRandomMap(Long seed) {
        Random r = new Random(seed);
        for (int blockY = 0; blockY < mapSizeBlocks; blockY++) {
            for (int blockX = 0; blockX < mapSizeBlocks; blockX++) {
                blocks[blockY][blockX] = new GrassBlock(r, blockSizeTiles, Config.tileSize, blockX, blockY);
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
