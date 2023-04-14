package Game;

import Controller.Controller;
import GameMap.GameMap;
import UI.VkFrame;
import View.LocalPlayerView;

import java.util.ArrayList;
import java.util.List;

public final class Game {

    private UI.VkFrame UI;
    private final GameMap gameMap;
    private Controller controller;

    private final List<Object> actions = new ArrayList<>();

    public Game(int mapSize, int blockSize) {
        gameMap = new GameMap(mapSize, blockSize);
        gameMap.generateRandomMap(System.currentTimeMillis());

        Controller controller = new Controller(gameMap, actions, 100, 100);

        UI = new VkFrame("new game", controller);

    }


}
