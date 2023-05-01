package Game;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.Blocks.Structures.House;
import GameMap.Blocks.Structures.Temple;
import GameMap.GameMap;
import UI.VkFrame;
import View.LocalPlayerView;
import VkRender.Config;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class Game {

    public final class CameraSelectionAdapter extends MouseAdapter {

        private boolean isSelecting = false;
        private Point selectionStartingPoint = null;
        private final Rectangle selectionRect = new Rectangle();

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null && e.getButton() == MouseEvent.BUTTON1) {
                isSelecting = true;
                selectionStartingPoint = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (isSelecting && e != null) {
                isSelecting = false;
                localPlayerView.getVkUI().select(e.getPoint(), e.getPoint());
//                UI.repaintCanvas();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (isSelecting && e != null) {
                selectionRect.setFrameFromDiagonal(selectionStartingPoint, e.getPoint());
                localPlayerView.getVkUI().select(selectionStartingPoint, e.getPoint());
            }
        }


        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e != null) {
                Block block = localPlayerView.getBlockByMouseClick(e.getPoint());

//                System.out.println(block);
                if (block != null) {
//                    System.out.println(block.getStructure());
                    try {
                        new House().build(gameMap, actions, localPlayerView.getBlockPositionByClick(e.getPoint()));
                    } catch (Throwable err) {
                        System.out.println("Cannot build house: " + err);
                    }
                }
            }
        }
    }
    public final class CameraMoveAdapter extends MouseAdapter {

        private boolean isMoving = false;
        private Point moveStartPoint = null;

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null && e.getButton() == MouseEvent.BUTTON2) {
                isMoving = true;
                moveStartPoint = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (isMoving && e != null && e.getButton() == MouseEvent.BUTTON2) {
                isMoving = false;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (isMoving && e != null) {
                Point delta = new Point(e.getPoint().x - moveStartPoint.x, e.getPoint().y - moveStartPoint.y);
                localPlayerView.getCamera().move(delta.x, delta.y);
                moveStartPoint = e.getPoint();
//                UI.repaintCanvas();
            }
        }

    }
    public final class CameraZoomAdapter extends MouseAdapter {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);
            if (e != null) {
                if (e.getPreciseWheelRotation() < 0) {
                    localPlayerView.getCamera().zoomIn();
//                    UI.repaintCanvas();
                } else if (e.getPreciseWheelRotation() > 0) {
                    localPlayerView.getCamera().zoomOut();
//                    UI.repaintCanvas();
                }
            }
        }
    }

    public final class BlockHightlightAdapter extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if (e != null) {
                Block block = localPlayerView.getBlockByMouseClick(e.getPoint());
                localPlayerView.getVkUI().highlightBlock(block);
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
    private ArrayList<Action> actions = new ArrayList<>();

    public Game(int mapSize, int blockSize) {
        gameMap = new GameMap(mapSize, blockSize);
        gameMap.generateRandomMap(System.currentTimeMillis());
//        gameMap.getBlock(1, 1).placeStructure(new Temple());
        Temple temple = new Temple();
        temple.build(gameMap, actions, new Point(1, 1));
        House house = new House();
        house.build(gameMap, actions, new Point(0, 0));

        localPlayerView = new LocalPlayerView(gameMap, new Point(0, 0));
        UI = new VkFrame("new game", localPlayerView);
        localPlayerView.setUI(UI);
        MouseAdapter moveAdapter = new CameraMoveAdapter();
        MouseAdapter selectionAdapter = new CameraSelectionAdapter();
        UI.getCanvas().addMouseWheelListener(new CameraZoomAdapter());
        UI.getCanvas().addMouseListener(moveAdapter);
        UI.getCanvas().addMouseMotionListener(moveAdapter);
        UI.getCanvas().addMouseListener(selectionAdapter);
        UI.getCanvas().addMouseMotionListener(selectionAdapter);
        UI.getCanvas().addMouseMotionListener(new BlockHightlightAdapter());
        UI.start();
        try {
            TimeUnit.SECONDS.sleep(1);
            run();
        } catch (Throwable t) {
            System.out.println("Error: " + t.getMessage());
        }
    }

    void run() throws InterruptedException {

        ArrayList<Action> newActions = new ArrayList<Action>();

        while(true) {
            newActions.clear();
            synchronized (actions) {
                for (Action action : actions) {
                    try {
                        action.execute(gameMap, newActions);
                    } catch (Throwable e) {
                        System.out.println("Error while doing action " + action + ": " + e);
                    }
                }
            }
            actions.clear();
            actions.addAll(newActions);
//            UI.getFrame().update(UI.getFrame().getGraphics());
            UI.repaintCanvas();
        }

    }
}
