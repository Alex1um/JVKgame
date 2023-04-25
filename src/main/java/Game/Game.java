package Game;

import GameMap.Blocks.Structures.Temple;
import GameMap.GameMap;
import UI.VkFrame;
import View.LocalPlayerView;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

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
                UI.repaintCanvas();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (isSelecting && e != null) {
                Canvas canvas = UI.getCanvas();
                Graphics g = canvas.getGraphics();
                UI.repaintCanvas();
                selectionRect.setFrameFromDiagonal(selectionStartingPoint, e.getPoint());
                g.setColor(new Color(0f, 1f, 0f, 0.4f));
                g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
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
                UI.repaintCanvas();
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
                    UI.repaintCanvas();
                } else if (e.getPreciseWheelRotation() > 0) {
                    localPlayerView.getCamera().zoomOut();
                    UI.repaintCanvas();
                }
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
        gameMap.getBlock(1, 1).placeStructure(new Temple());
        localPlayerView = new LocalPlayerView(gameMap, new Point(0, 0), 100);
        UI = new VkFrame("new game", localPlayerView);
        MouseAdapter moveAdapter = new CameraMoveAdapter();
        MouseAdapter selectionAdapter = new CameraSelectionAdapter();
        UI.getCanvas().addMouseWheelListener(new CameraZoomAdapter());
        UI.getCanvas().addMouseListener(moveAdapter);
        UI.getCanvas().addMouseMotionListener(moveAdapter);
        UI.getCanvas().addMouseListener(selectionAdapter);
        UI.getCanvas().addMouseMotionListener(selectionAdapter);
        UI.start();
    }


}
