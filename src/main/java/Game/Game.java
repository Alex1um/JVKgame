package Game;

import Controller.Controller;
import GameMap.Blocks.Block;
import GameMap.Blocks.Structures.Temple;
import GameMap.GameMap;
import UI.VkFrame;
import View.LocalPlayerView;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

public final class Game {

    public final class AreaMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null) {
                int button = e.getButton();
                if (button == MouseEvent.BUTTON1) {
                    localPlayerView.setSelectionStartingPoint(e.getPoint());
                    localPlayerView.getSelectionRect().setRect(e.getX(), e.getY(), 0, 0);
                } else if (button == MouseEvent.BUTTON2) {
                    localPlayerView.setCameraMovementStartingPoint(e.getPoint());
                    localPlayerView.setCameraStartingPoint(localPlayerView.getCameraCoords());
                    localPlayerView.setCameraMoving(true);
                }
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e != null) {
                int button = e.getButton();
                if (button == MouseEvent.BUTTON1) {
                    localPlayerView.setSelectionStartingPoint(null);
                } else if (button == MouseEvent.BUTTON2) {
                    localPlayerView.setCameraMovementStartingPoint(null);
                    localPlayerView.setCameraStartingPoint(null);
                    localPlayerView.setCameraMoving(false);
                }
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e != null) {
                Point mouse = e.getPoint();
                Rectangle camera = localPlayerView.getCamera_rect_tiles();
                Point relCords = new Point(mouse.x + camera.x, mouse.y + camera.y);
                System.out.println(String.valueOf(camera.x) + " " + String.valueOf(camera.y));
                // TODO add check
                System.out.print((relCords.x / gameMap.getSize() / 20));
                System.out.print(" ");
                System.out.println(relCords.x / gameMap.getSize() / 20);
                Block block = gameMap.getBlock(relCords.x / gameMap.getSize() / 20, relCords.y / gameMap.getSize() / 20);
                block.placeStructure(new Temple());
            }
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            if (e != null) {
                if (localPlayerView.getSelectionStartingPoint() != null) {
                    localPlayerView.getSelectionRect().setFrameFromDiagonal(localPlayerView.getSelectionStartingPoint(), e.getPoint());
                }
                if (localPlayerView.isCameraMoving()) {
                    localPlayerView.setCameraCoords(
                            localPlayerView.getCameraStartingPoint().x - e.getX() + localPlayerView.getCameraStartingPoint().x,
                            localPlayerView.getCameraStartingPoint().y - e.getY() + localPlayerView.getCameraStartingPoint().y
                    );
                }
            }
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e != null) {
                localPlayerView.scale(e.getPreciseWheelRotation());
                localPlayerView.setCameraScaled(true);
            }
        }
    }

    // View
    public LocalPlayerView getLocalPlayerView() {
        return localPlayerView;
    }

    private LocalPlayerView localPlayerView;

    private UI.VkFrame UI;
    private final GameMap gameMap;
    private final List<Object> actions = new ArrayList<>();

    public Game(int mapSize, int blockSize) {
        gameMap = new GameMap(mapSize, blockSize);
        gameMap.generateRandomMap(System.currentTimeMillis());
        localPlayerView = new LocalPlayerView(0, 0, blockSize, blockSize, 20);
        localPlayerView.generateVertices(gameMap);
        AreaMouseAdapter mouseAdapter = new AreaMouseAdapter();
        UI = new VkFrame("new game", mouseAdapter, localPlayerView);

    }


}
