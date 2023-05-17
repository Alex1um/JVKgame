package GameMap.GameObjects.Units;

import Game.Abilities.AbilityMethod;
import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.GameObject;
import GameMap.GameObjects.Structures.Structure;
import GameMap.Tiles.Tile;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
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
    UnitStats stats;

    protected Unit(float maxHealth, UnitStats stats) {
        super(maxHealth);
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

        public void moveOneStep(GameMap gameMap) {
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
        public void moveActionF(GameMap gameMap, ArrayList<Action> newActions) {
            moveOneStep(gameMap);
            if (hasNextStep())
                newActions.add(currentAction);
        }
    }

    private final AStarPathfinding2 pathing = new AStarPathfinding2();

    private final Action movingAction = new Action(pathing::moveActionF);
    private final Action attackAction = new Action(this::attackMove);
    @Nullable
    private Action currentAction = null;
    @AbilityMethod(name = "move")
    public void move(GameMap gameMap, ArrayList<Action> actions, Object... args) {
        Point destination = (Point) args[0];
        currentAction = movingAction;
        if (!pathing.hasNextStep()) {
            pathing.createPath(gameMap, tilePosition, destination);
            actions.add(currentAction);
        } else {
            pathing.createPath(gameMap, tilePosition, destination);
        }
    }

    @Nullable
    private GameObject attackObject = null;

    @Nullable
    private Instant attackStartTime = null;

    private void attackMove(GameMap gameMap, ArrayList<Action> actions) {
        if (attackObject == null) {
            GameObject newTarget = scanForTargets(gameMap);
            if (newTarget != null) {
                attackObject = newTarget;
            } else if (pathing.hasNextStep()) {
                pathing.moveOneStep(gameMap);
            }
        } else {
            Point targetPos = null;

            if (attackObject instanceof Unit) {
                targetPos = ((Unit) attackObject).tilePosition;
            } else if (attackObject instanceof Structure) {
                targetPos = new Point(((Structure) attackObject).getBlockPosition());
                targetPos.x *= gameMap.getBlockSizeTiles();
                targetPos.y *= gameMap.getBlockSizeTiles();
                if (tilePosition.y > targetPos.y) {
                    targetPos.y += gameMap.getBlockSizeTiles();
                }
                if (tilePosition.x > targetPos.x) {
                    targetPos.x += gameMap.getBlockSizeTiles();
                }
            }
            int distance = Math.abs(tilePosition.y - targetPos.y) + Math.abs(tilePosition.x - targetPos.x);
            if (distance > stats.attackRange) {
                if (!targetPos.equals(pathing.movingDestination)) {
                    pathing.createPath(gameMap, tilePosition, targetPos);
                }
                pathing.moveOneStep(gameMap);
            } else {
//                if (Duration.between(attackStartTime))
                if (attackStartTime == null) {
                    attackStartTime = Instant.now();
                } else if (Duration.between(attackStartTime, Instant.now()).getNano() > stats.attackInterval.getNano()) {
                    attackStartTime.plus(stats.attackInterval);
                    attackObject.damage(stats.attack);
                    if (attackObject.getHealth() <= 0) {
                        gameMap.objects.remove(attackObject);
                        if (attackObject instanceof Unit) {
                            gameMap.getTile(((Unit) attackObject).tilePosition).setUnit(null);
                        } else if (attackObject instanceof Structure) {
                            gameMap.getBlockByTilePos(((Structure) attackObject).getBlockPosition()).setStructure(null);
                        }
                    }
                }
            }
        }
        actions.add(currentAction);
    }

    @Nullable
    private GameObject scanForTargets(GameMap gameMap) {
        for (int distance = 1; distance <= stats.attackScanRange; distance++) {
            for (int i = -distance; i <= distance; i++) {
                for (int j = -distance; j <= distance; j++) {
                    // Пропускаем клетки, которые находятся дальше, чем distance
                    if (Math.abs(i) + Math.abs(j) > distance || i == 0 && j == 0) {
                        continue;
                    }
                    int x = tilePosition.x + i;
                    int y = tilePosition.y + j;
                    Unit unit = gameMap.getTile(x, y).getUnit();
                    if (unit != null) {
                        return unit;
                    }
                    Structure structure = gameMap.getBlockByTilePos(x, y).getStructure();
                    if (structure != null) {
                        return structure;
                    }
                }
            }
        }
        return null;
    }

    @AbilityMethod(name = "attack")
    public void attack(GameMap gameMap, ArrayList<Action> actions, Object... args) {
        Point target = args.length > 0 ? (Point) args[0] : null;
        currentAction = attackAction;
        if (!pathing.hasNextStep()) {
            actions.add(currentAction);
        }
        if (target != null) {
            pathing.createPath(gameMap, tilePosition, target);
            attackObject = gameMap.getObjectByTilePos(target);
        }
    }

}
