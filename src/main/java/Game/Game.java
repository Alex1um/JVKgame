package Game;

import Controller.Players.BotPlayer;
import Controller.Players.LocalPlayer;
import Controller.Players.Player;
import Game.Abilities.Ability;
import Game.Abilities.BasicAbility;
import Game.Abilities.TargetAbility;
import Game.Actions.Action;
import GameMap.GameObjects.GameObject;
import GameMap.GameObjects.Units.Necromancer;
import GameMap.GameMap;
import GameMap.GameObjects.Units.Unit;
import GameMap.Tiles.Tile;
import UI.VkFrame;
import Frame.VkGame;
import View.LocalPlayerView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public final class Game {

    @Nullable
    String clickModifier = null;

    private final ArrayList<GameObject> selectedObjects = new ArrayList<>();

    public final class CameraSelectionAdapter extends MouseAdapter {

        private boolean isSelecting = false;
        private Point selectionStartingPoint = null;
        private final Rectangle selectionRect = new Rectangle();

        private void deselect() {
            UI.deselect();
            for (GameObject obj : selectedObjects) {
                if (gameMap.objects.contains(obj)) {
                    localPlayerView.getGameObjectsView().getObjectView(obj).highlight(0);
                }
            }
            selectedObjects.clear();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null && e.getButton() == MouseEvent.BUTTON1 && clickModifier == null) {
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
                            UI.updateSelectedObjects(selectedObjects);
//                            UI.selectObject(unit);
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
                    if (clickModifier != null) {
                        if (!selectedObjects.isEmpty()) {
                            Point attackPoint = localPlayerView.getTilePositionByClick(e.getPoint());
                            for (GameObject obj : selectedObjects) {
                                if (obj instanceof Unit) {
                                    if (obj.getAbilities() != null && obj.getAbilities().get(clickModifier) != null) {
                                        obj.getAbilities().get(clickModifier).use(gameMap, actions, attackPoint);
                                    }
                                }
                            }
                        }
                        clickModifier = null;
                    } else {
                        deselect();
                        GameObject obj = localPlayerView.getObjectByMouseClick(e.getPoint());
                        if (obj != null) {
                            selectedObjects.add(obj);
                            UI.updateSelectedObjects(selectedObjects);
//                            UI.selectObject(obj);
                            localPlayerView.getGameObjectsView().getObjectView(obj).highlight(1);
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
                        clickModifier = null;
                        for (GameObject obj : selectedObjects) {
                            if (obj instanceof Unit) {
                                obj.getAbilities().get("move").use(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
                            }
                        }
                    } else {
                        // TODO: remove
                        try {
                            new Necromancer(localPlayer).deploy(gameMap, actions, localPlayerView.getTilePositionByClick(e.getPoint()));
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
                clickModifier = "attack";
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

    public final class SkillTablebutton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent != null) {
                String abilityName = ((JButton)actionEvent.getSource()).getName();
                for (GameObject selectedObject : selectedObjects) {
                    Ability ability = selectedObject.getAbilities().get(abilityName);
                    if (ability != null) {
                        if (ability instanceof TargetAbility) {
                            clickModifier = abilityName;
                        } else if (ability instanceof BasicAbility) {
                            ability.use(gameMap, actions);
                        }
                    }
                }
            }
        }
    }

    // View
    private final LocalPlayerView localPlayerView;

    private final VkFrame UI;
    private final GameMap gameMap;
    private final ArrayList<Action> actions = new ArrayList<>();

    private final Player localPlayer = new LocalPlayer();

    private final ArrayList<Player> players = new ArrayList(1);

    public Game(int mapSize, int blockSize, int botCount) {
        gameMap = new GameMap(mapSize, blockSize);
        long seed = System.currentTimeMillis();
        gameMap.generateRandomMap(seed);

        players.add(localPlayer);
        for (;botCount > 0; botCount--) {
            BotPlayer bot = new BotPlayer();
            players.add(bot);
        }

        gameMap.initPlayers(seed, players, actions);

        ActionListener abilityButtonsListener = new SkillTablebutton();
        localPlayerView = new LocalPlayerView(gameMap, new Point(0, 0));
        UI = new VkFrame(localPlayerView, 60, () -> {
            run();
            return null;
        }, abilityButtonsListener);
        localPlayerView.setCanvas(UI.getCanvas());
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
