package Controller;

import GameMap.GameMap;
import View.LocalPlayerView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

// User input / output processing
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
                isCameraScaled = true;
            }
       }

    }

    // Camera scale
    private boolean isCameraScaled = false;
    public boolean isCameraScaled() {
        if (isCameraScaled) {
            isCameraScaled = false;
            return true;
        }
        return isCameraScaled;
    }

    // Camera movement
    @Nullable
    private Point cameraMovementStartingPoint = null;

    @Nullable
    private Point cameraStartingPoint = null;

    private boolean isCameraMoving = false;

    public boolean isCameraMoving() {
        return isCameraMoving;
    }

    // Selection
    @NotNull
    private final Rectangle selectionRect = new Rectangle();
    @NotNull
    public Rectangle getSelectionRect() {
        return selectionRect;
    }

    @Nullable
    Point selectionStartingPoint = null;

    public boolean isSelecting() {
        return selectionStartingPoint != null;
    }

    // ADapter

    private final AreaMouseAdapter areMouseAdapter = new AreaMouseAdapter();

    public AreaMouseAdapter getAreMouseAdapter() {
        return areMouseAdapter;
    }

    // View
    public LocalPlayerView getLocalPlayerView() {
        return localPlayerView;
    }

    private final LocalPlayerView localPlayerView;

    private final List<Object> actionsBuffer;

    public Controller(GameMap map, List<Object> actionsBuffer, int widthCells, int heightCells) {
        this.actionsBuffer = actionsBuffer;
        localPlayerView = new LocalPlayerView(0, 0, widthCells, heightCells, 20);
        generatePlayerView(map);
    }

    void generatePlayerView(GameMap map) {
        localPlayerView.generateVertices(map);
        localPlayerView.generateIndexes(map);
    }

}
