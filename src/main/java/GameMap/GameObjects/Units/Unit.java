package GameMap.GameObjects.Units;

import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.Tiles.Tile;
import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Unit extends GameObject {

    public void setTilePosition(Tile newTile, Point tilePosition) {
        this.tilePosition = tilePosition;
    }

    public Point getTilePosition() {
        return tilePosition;
    }

    private Point tilePosition;
    final int textureIndex;

    UnitStats stats;

    protected Unit(int textureIndex, float maxHealth, UnitStats stats) {
        super(maxHealth);
        this.textureIndex = textureIndex;
        this.stats = stats;
    }

    public void deploy(GameMap gameMap, ArrayList<Action> actions, Point tilePosition) {
        this.tilePosition = tilePosition;
        actions.add(new Action((this::create)));
    }

    private void create(GameMap gameMap, ArrayList<Action> actions) {
        Tile tile = gameMap.getTile(this.tilePosition);
        Block block = gameMap.getBlockByTilePos(this.tilePosition);
        if (block.getStructure() == null && tile.getUnit() == null) {
            tile.setUnit(this);
            gameMap.objects.add(this);
        } else {
            throw new Error("Cannot place unit: Tile already occupied " + tilePosition);
        }
    }

    class AStarPathfinding2 {

        HashMap<Point, Point> path = new HashMap<>();
        @Nullable
        private Point movingDestination = null;

        private float traveled = 0f;

        public void createPath(GameMap gameMap, Point start, Point goal) {
            HashMap<Point, Point> cameFrom = new HashMap<>();
            movingDestination = goal;
            HashMap<Point, Float> costSoFar = new HashMap<>();
            PriorityQueue<Point> frontier = new PriorityQueue<>(Comparator.comparingDouble(o -> costSoFar.getOrDefault(o, Float.POSITIVE_INFINITY) + heuristic(goal, o)));
            frontier.offer(start);
            cameFrom.put(start, null);
            costSoFar.put(start, 0.0f);

            while (!frontier.isEmpty()) {
                Point current = frontier.poll();

                if (current.equals(goal)) {
                    break;
                }

                for (Point next : getNeighbors(gameMap, current)) {
                    float newCost = costSoFar.get(current) + getCost(gameMap, current, next);
                    if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                        costSoFar.put(next, newCost);
//                        double priority = newCost + heuristic(goal, next);
                        frontier.offer(next);
                        cameFrom.put(next, current);
                    }
                }
            }
            path.clear();
            Point current = goal;
            while (!current.equals(start)) {
                path.put(cameFrom.get(current), current);
                current = cameFrom.get(current);
            }

        }

        private List<Point> getNeighbors(GameMap gameMap, Point p) {
            List<Point> neighbors = new ArrayList<>();
            Point[] candidates = new Point[] {
                    new Point(p.x - 1, p.y),
                    new Point(p.x + 1, p.y),
                    new Point(p.x, p.y - 1),
                    new Point(p.x, p.y + 1)
            };

            for (Point neighbor : candidates) {
                if (gameMap.isTilePosValid(neighbor) && (gameMap.getBlockByTilePos(neighbor).getStructure() == null || neighbor.equals(movingDestination))) {
                    neighbors.add(neighbor);
                }
            }

            return neighbors;
        }
        private float getCost(GameMap gameMap, Point from, Point to) {
            if (gameMap.getBlockByTilePos(to).getStructure() != null) {
                return Float.POSITIVE_INFINITY;
            }
            Tile tile = gameMap.getTile(to);

            float fact = 1f / tile.getMSFactor();
            if (tile.getUnit() != null) {
                return fact * 2 + 20;
            } else {
                return fact;
            }
        }
        private double heuristic(Point a, Point b) {
            return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        }
        public boolean hasNextStep() {
            return tilePosition != movingDestination && path.get(tilePosition) != null;
        }
        public void moveOneStep(GameMap gameMap, ArrayList<Action> newActions) {
            if (hasNextStep())
                newActions.add(movingAction);
            traveled += stats.speedTilesPerFrame * gameMap.getTile(tilePosition).getMSFactor();
            for (int i = (int) traveled; i > 0; i--) {
                Point next = path.get(tilePosition);
                if (next == null) {
                    return;
                }
                if (gameMap.isTilePositionFree(next)) {
                    Tile newTile = gameMap.getTile(next);
                    Tile oldTile = gameMap.getTile(tilePosition);
                    oldTile.setUnit(null);
                    newTile.setUnit(Unit.this);
                    setTilePosition(newTile, next);
                }
            }
            traveled %= 1;
        }
    }

    private final AStarPathfinding2 pathing = new AStarPathfinding2();

    @Nullable
    private Action movingAction = new Action(pathing::moveOneStep);

    @AbilityMethod(name = "move")
    public void move(GameMap gameMap, ArrayList<Action> actions, Object... args) {
        Point destination = (Point) args[0];
        if (!pathing.hasNextStep()) {
            pathing.createPath(gameMap, tilePosition, destination);
            actions.add(movingAction);
        } else {
            pathing.createPath(gameMap, tilePosition, destination);
        }
    }

}
