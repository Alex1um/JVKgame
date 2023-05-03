package Game;

import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameObjects.GameObject;
import GameMap.GameObjects.Structures.House;
import GameMap.GameObjects.Units.Master;
import GameMap.GameMap;
import GameMap.GameObjects.Structures.Temple;
import GameMap.GameObjects.Units.Unit;
import GameMap.Tiles.Tile;
import UI.VkFrame;
import View.LocalPlayerView;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

public final class Game {

    public final class CameraSelectionAdapter extends MouseAdapter {

        private boolean isSelecting = false;
        private Point selectionStartingPoint = null;
        private final Rectangle selectionRect = new Rectangle();
        private final ArrayList<GameObject> selectedObjects = new ArrayList<>();

        private void deselect() {
            for (GameObject obj : selectedObjects) {
                localPlayerView.getVkUI().setObjecthighlight(obj, 0);
            }
            selectedObjects.clear();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null && e.getButton() == MouseEvent.BUTTON1) {
                isSelecting = true;
                deselect();
                selectionStartingPoint = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (isSelecting && e != null) {
                isSelecting = false;
                localPlayerView.getVkUI().select(e.getPoint(), e.getPoint());

                Point p1 = localPlayerView.getTilePositionByClick(selectionStartingPoint);
                Point p2 = localPlayerView.getTilePositionByClick(e.getPoint());
                int incX = Integer.signum(p2.x - p1.x);
                int incY = Integer.signum(p2.y - p1.y);

                for (int y = p1.y; y != p2.y; y += incY) {
                    for (int x = p1.x; x != p2.x; x += incX) {
                        Tile tile = gameMap.getTile(x, y);
                        if (tile != null && tile.getUnit() != null) {
                            Unit unit = tile.getUnit();
                            selectedObjects.add(unit);
                            localPlayerView.getVkUI().setObjecthighlight(unit, 1);
                        }
                    }
                }

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
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Tile tile = localPlayerView.getTileByMouseClick(e.getPoint());
                    if (tile != null) {
                        Unit unit = tile.getUnit();
                        if (unit != null) {
                            selectedObjects.add(unit);
                            localPlayerView.getVkUI().setObjecthighlight(unit, 1);
                        } else {
                            deselect();
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    Tile tile = localPlayerView.getTileByMouseClick(e.getPoint());
                    if (tile != null) {
                        Unit unit = tile.getUnit();
                        if (unit instanceof Master) {
                            unit.getAbilities().get(0).use(actions);
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (selectedObjects.isEmpty()) {
                        try {
                            new Master().deploy(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
                        } catch (Throwable err) {
                            System.out.println("Cannot deploy unit house: " + err);
                        }
                    } else {
                        for (GameObject obj : selectedObjects) {
                            if (obj instanceof Unit) {
                                ((Unit) obj).move(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
                            }
                        }
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
                localPlayerView.getVkUI().setBlockhighlight(block, 1);
            }
        }
    }
    // View
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
        UI = new VkFrame("new game", localPlayerView, 60, () -> {
            run();
            return null;
        });
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
//        try {
//            TimeUnit.SECONDS.sleep(1);
//            run();
//        } catch (Throwable t) {
//            System.out.println("Error: " + t.getMessage());
//        }
    }

    void run() {

        ArrayList<Action> newActions = new ArrayList<Action>();

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

        UI.repaintCanvas();

    }
}
