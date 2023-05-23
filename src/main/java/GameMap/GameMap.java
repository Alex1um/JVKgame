package GameMap;

import Controller.Players.Player;
import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.Blocks.GoldMineBlock;
import GameMap.Blocks.GrassBlock;
import GameMap.Blocks.WaterBlock;
import GameMap.GameObjects.GameObject;
import GameMap.GameObjects.Structures.Structure;
import GameMap.GameObjects.Structures.Temple;
import GameMap.GameObjects.Units.Unit;
import GameMap.Tiles.Tile;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;

import java.awt.*;
import java.util.ArrayList;

public class GameMap {

    public Block[][] getBlocks() {
        return blocks;
    }
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
        Random random = new Random(seed);

        int numWaterGroups = mapSizeBlocks; // Количество групп клеток воды
        int numSmallGoldGroups = mapSizeBlocks / 5; // Количество мелких групп клеток шахт
        for (int i = 0; i < mapSizeBlocks; i++) {
            for (int j = 0; j < mapSizeBlocks; j++) {
                if (random.nextInt(10) < 9) { // Увеличиваем вероятность земли
                    blocks[i][j] = new GrassBlock(blockSizeTiles);
                } else {
                    blocks[i][j] = new WaterBlock(blockSizeTiles);
                }
            }
        }

        // Появление групп клеток воды
        while (numWaterGroups > 0) {
            int groupId = random.nextInt(1000) + 1; // Генерируем ID группы
            int size = random.nextInt(3) + 2; // Генерируем размер группы
            boolean validGroup = true;
            int x = random.nextInt(mapSizeBlocks);
            int y = random.nextInt(mapSizeBlocks - size + 1);
            for (int i = 0; i < size; i++) {
                if (!(blocks[x][y + i] instanceof GrassBlock)) { // Проверяем, что все клетки в группе находятся на земле
                    validGroup = false;
                    break;
                }
            }
            if (validGroup) { // Добавляем группу клеток воды на карту
                for (int i = 0; i < size; i++) {
                    blocks[x][y + i] = new WaterBlock(blockSizeTiles);
                }
                numWaterGroups--;
            }
        }

        // Появление групп клеток шахт
        while (numSmallGoldGroups > 0) {
            int groupId = random.nextInt(1000) + 1; // Генерируем ID группы
            int size = random.nextInt(3) + 2; // Генерируем размер группы
            boolean validGroup = true;
            int x = random.nextInt(mapSizeBlocks - size + 1);
            int y = random.nextInt(mapSizeBlocks - size + 1);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (!(blocks[x + i][y + j] instanceof GrassBlock)) { // Проверяем, что все клетки в группе находятся на земле
                        validGroup = false;
                        break;
                    }
                }
            }
            if (validGroup) { // Добавляем группу клеток шахт на карту
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        blocks[x + i][y + j] = new GoldMineBlock(blockSizeTiles, random);
                    }
                }
                numSmallGoldGroups--;
            }
        }

        // Выделяем случайные клетки с травой для каждого игрока
    }

    public void initPlayers(Long seed, ArrayList<Player> players, ArrayList<Action> actions) {
        Random random = new Random(seed);
        for (Player player : players) {
            int x = random.nextInt(mapSizeBlocks);
            int y = random.nextInt(mapSizeBlocks);
            while (!(blocks[x][y] instanceof GrassBlock)) { // Проверяем, что клетка соответствует земле
                x = random.nextInt(mapSizeBlocks);
                y = random.nextInt(mapSizeBlocks);
            }
            new Temple(player).build(this, actions, new Point(x, y));
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
    public boolean isBlockPositionFree(Point pos) {
        Block block = this.getBlockByTilePos(pos);
        if (block.getStructure() != null) {
            return false;
        } else {
            for (Tile[] tileRow : block.getTiles()) {
                for (Tile tile : tileRow) {
                    if (tile.getUnit() != null) return false;
                }
            }
        }
        return true;
    }

    @Nullable
    public GameObject getObjectByTilePos(int tileX, int tileY) {
        Unit unit = getTile(tileX, tileY).getUnit();
        if (unit != null) return unit;
        Structure struct = getBlockByTilePos(tileX, tileY).getStructure();
        return struct;
    }

    @Nullable
    public GameObject getObjectByTilePos(Point pos) {
        return getObjectByTilePos(pos.x, pos.y);
    }
}
