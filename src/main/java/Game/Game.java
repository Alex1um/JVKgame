package Game;

import Controller.Controller;
import GameMap.GameMap;
import UI.VkFrame;
import View.LocalPlayerView;

public final class Game {

    private UI.VkFrame UI;
    private final GameMap gameMap;
    private Controller controller;

    public Game(int mapSize, int blockSize) {
        gameMap = new GameMap(mapSize, blockSize);
        gameMap.generateRandomMap(System.currentTimeMillis());

        Controller controller = new Controller(gameMap, 100, 100);

        UI = new VkFrame("new game", controller);

//        UI.applyView(view);
//        UI.paint(UI.getGraphics());
    }


}
