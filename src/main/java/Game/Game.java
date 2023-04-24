package Game;

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

//    public final class AreaMouseAdapter extends MouseAdapter {
//
//        @Override
//        public void mousePressed(MouseEvent e) {
//            super.mousePressed(e);
//            if (e != null) {
//                int button = e.getButton();
//                if (button == MouseEvent.BUTTON1) {
//                    localPlayerView.setSelectionStartingPoint(e.getPoint());
//                    localPlayerView.getSelectionRect().setRect(e.getX(), e.getY(), 0, 0);
//                } else if (button == MouseEvent.BUTTON2) {
//                    localPlayerView.setCameraMovementStartingPoint(e.getPoint());
//                    localPlayerView.setCameraStartingPoint(localPlayerView.getCameraCoords());
//                    localPlayerView.setCameraMoving(true);
//                }
//            }
//        }
//        @Override
//        public void mouseReleased(MouseEvent e) {
//            if (e != null) {
//                int button = e.getButton();
//                if (button == MouseEvent.BUTTON1) {
//                    localPlayerView.setSelectionStartingPoint(null);
//                } else if (button == MouseEvent.BUTTON2) {
//                    localPlayerView.setCameraMovementStartingPoint(null);
//                    localPlayerView.setCameraStartingPoint(null);
//                    localPlayerView.setCameraMoving(false);
//                }
//            }
//        }
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            if (e != null) {
//                Point mouse = e.getPoint();
//                Rectangle camera = localPlayerView.getCamera_rect_tiles();
//                Point relCords = new Point(mouse.x + camera.x, mouse.y + camera.y);
//                System.out.println(String.valueOf(camera.x) + " " + String.valueOf(camera.y));
//                // TODO add check
//                System.out.print((relCords.x / gameMap.getSize() / 20));
//                System.out.print(" ");
//                System.out.println(relCords.x / gameMap.getSize() / 20);
//                Block block = gameMap.getBlock(relCords.x / gameMap.getSize() / 20, relCords.y / gameMap.getSize() / 20);
//                block.placeStructure(new Temple());
//            }
//        }
//        @Override
//        public void mouseDragged(MouseEvent e) {
//            if (e != null) {
//                if (localPlayerView.getSelectionStartingPoint() != null) {
//                    localPlayerView.getSelectionRect().setFrameFromDiagonal(localPlayerView.getSelectionStartingPoint(), e.getPoint());
//                }
//                if (localPlayerView.isCameraMoving()) {
//                    localPlayerView.setCameraCoords(
//                            localPlayerView.getCameraStartingPoint().x - e.getX() + localPlayerView.getCameraStartingPoint().x,
//                            localPlayerView.getCameraStartingPoint().y - e.getY() + localPlayerView.getCameraStartingPoint().y
//                    );
//                }
//            }
//        }
//        @Override
//        public void mouseWheelMoved(MouseWheelEvent e) {
//            if (e != null) {
//                localPlayerView.scale(e.getPreciseWheelRotation());
//                localPlayerView.setCameraScaled(true);
//            }
//        }
//    }

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
                moveStartPoint = null;
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
                } else if (e.getPreciseWheelRotation() > 0) {
                    localPlayerView.getCamera().zoomOut();
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
        localPlayerView = new LocalPlayerView(gameMap, new Point(0, 0), 100);
        UI = new VkFrame("new game", localPlayerView);
        MouseAdapter moveAdapter = new CameraMoveAdapter();
        UI.getCanvas().addMouseWheelListener(new CameraZoomAdapter());
        UI.getCanvas().addMouseListener(moveAdapter);
        UI.getCanvas().addMouseMotionListener(moveAdapter);
        UI.start();
    }


}
