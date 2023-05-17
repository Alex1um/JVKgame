package Game;

import Game.Actions.Action;
import GameMap.GameObjects.GameObject;
import GameMap.GameObjects.Units.Necromancer;
import GameMap.GameMap;
import GameMap.GameObjects.Units.Unit;
import GameMap.Tiles.Tile;
import UI.VkFrame;
import View.LocalPlayerView;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public final class Game {

    enum ClickModifier {
        None,
        Attack,
    }
    ClickModifier clickModifier = ClickModifier.None;

    public final class CameraSelectionAdapter extends MouseAdapter {

        private boolean isSelecting = false;
        private Point selectionStartingPoint = null;
        private final Rectangle selectionRect = new Rectangle();
        private final ArrayList<GameObject> selectedObjects = new ArrayList<>();

        private void deselect() {
            for (GameObject obj : selectedObjects) {
                localPlayerView.getGameObjectsView().getObjectView(obj).highlight(0);
            }
            selectedObjects.clear();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null && e.getButton() == MouseEvent.BUTTON1 && clickModifier == ClickModifier.None) {
                isSelecting = true;
                selectionStartingPoint = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (isSelecting && e != null) {
                deselect();
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
                            localPlayerView.getGameObjectsView().getObjectView(unit).highlight(1);
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
                    switch (clickModifier) {
                        case Attack: {
                            if (!selectedObjects.isEmpty()) {
                                Point attackPoint = localPlayerView.getTilePositionByClick(e.getPoint());
                                for (GameObject obj : selectedObjects) {
                                    if (obj instanceof Unit) {
                                        obj.getAbilities().get("attack").use(gameMap, actions, attackPoint);
                                    }
                                }
                                clickModifier = ClickModifier.None;
                                break;
                            }
                            clickModifier = ClickModifier.None;
                        }
                        case None: {
                            deselect();
                            GameObject obj = localPlayerView.getObjectByMouseClick(e.getPoint());
                            if (obj != null) {
                                selectedObjects.add(obj);
                                localPlayerView.getGameObjectsView().getObjectView(obj).highlight(1);
                            }
                            break;
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    // TODO: remove
                    Tile tile = localPlayerView.getTileByMouseClick(e.getPoint());
                    if (tile != null) {
                        Unit unit = tile.getUnit();
                        if (unit instanceof Necromancer) {
                            unit.getAbilities().get("Summon slave!").use(gameMap, actions);
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!selectedObjects.isEmpty()) {
                        clickModifier = ClickModifier.None;
                        for (GameObject obj : selectedObjects) {
                            if (obj instanceof Unit) {
                                obj.getAbilities().get("move").use(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
                            }
                        }
                    } else {
                        // TODO: remove
                        try {
                            new Necromancer().deploy(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
                        } catch (Throwable err) {
                            System.out.println("Cannot deploy unit house: " + err);
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

    public final class ModifierListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent keyEvent) {
            if (keyEvent.getKeyChar() == 'a') {
                clickModifier = ClickModifier.Attack;
                System.out.println("A pressed");
            }
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {

        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

        }
    }
    // View
    private final LocalPlayerView localPlayerView;

    private final UI.VkFrame UI;
    private final GameMap gameMap;
    private final ArrayList<Action> actions = new ArrayList<>();

    public Game(int mapSize, int blockSize) {
        gameMap = new GameMap(mapSize, blockSize);
        gameMap.generateRandomMap(System.currentTimeMillis());

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
        UI.getCanvas().addKeyListener(new ModifierListener());
        UI.start();
    }

    void run() {

        ArrayList<Action> newActions = new ArrayList<>();

        for (Action action : actions) {
            try {
                action.execute(gameMap, newActions);
            } catch (Throwable e) {
                System.out.println("Error while doing action " + action + ": " + e);
            }
        }
        actions.clear();
        actions.addAll(newActions);

        UI.repaintCanvas();

    }
}
