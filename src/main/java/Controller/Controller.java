package Controller;

import GameMap.GameMap;
import View.LocalPlayerView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class Controller {

    public final class AreaMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            if (e != null) {
                int button = e.getButton();
                if (button == MouseEvent.BUTTON1) {
                    selectionStartingPoint = e.getPoint();
                    selectionRect.setRect(e.getX(), e.getY(), 0, 0);
                } else if (button == MouseEvent.BUTTON2) {
                    cameraMovementStartingPoint = e.getPoint();
                    cameraStartingPoint = localPlayerView.getCameraCoords();
                    isCameraMoving = true;
                }
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e != null) {
                int button = e.getButton();
                if (button == MouseEvent.BUTTON1) {
                    selectionStartingPoint = null;
                } else if (button == MouseEvent.BUTTON2) {
                    cameraMovementStartingPoint = null;
                    cameraStartingPoint = null;
                    isCameraMoving = false;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e != null) {
                if (selectionStartingPoint != null) {
                    selectionRect.setFrameFromDiagonal(selectionStartingPoint, e.getPoint());
                }
                if (isCameraMoving) {
                    localPlayerView.setCameraCoords(
                            cameraStartingPoint.x - e.getX() + cameraMovementStartingPoint.x,
                            cameraStartingPoint.y - e.getY() + cameraMovementStartingPoint.y
                            );
                }
            }
        }

       @Override
       public void mouseWheelMoved(MouseWheelEvent e) {
            if (e != null) {
                localPlayerView.scale(e.getPreciseWheelRotation());
            }
       }

    }

    public AreaMouseAdapter getAreMouseAdapter() {
        return areMouseAdapter;
    }

    private final AreaMouseAdapter areMouseAdapter = new AreaMouseAdapter();

    @Nullable
    private Point cameraMovementStartingPoint = null;
    @Nullable
    private Point cameraStartingPoint = null;

    @NotNull
    public Rectangle getSelectionRect() {
        return selectionRect;
    }

    @NotNull
    private final Rectangle selectionRect = new Rectangle();

    @Nullable
    Point selectionStartingPoint = null;

    public boolean isSelecting() {
        return selectionStartingPoint != null;
    }

    public LocalPlayerView getLocalPlayerView() {
        return localPlayerView;
    }

    private final LocalPlayerView localPlayerView;

    public boolean isCameraMoving() {
        return isCameraMoving;
    }

    private boolean isCameraMoving = false;


    public Controller(GameMap map, int widthCells, int heightCells) {
        localPlayerView = new LocalPlayerView(0, 0, widthCells, heightCells, 20);
        generatePlayerView(map);
    }

    void generatePlayerView(GameMap map) {
        localPlayerView.generateVertices(map);
        localPlayerView.generateIndexes(map);
    }

}
