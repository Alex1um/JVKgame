package GameMap.GameObjects.Units;

import Controller.Players.Player;
import Game.Abilities.TargetAbilityMethod;
import Game.Actions.Action;
import GameMap.Blocks.Block;
import GameMap.GameMap;
import GameMap.GameObjects.Structures.House;
import GameMap.Tiles.GoldMineTile;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Worker extends Unit {
    public Worker(Player player) {
        super(player, 50, new UnitStats(1f, 1, 1, Duration.ofSeconds(2), 1));
    }

    private static final int buildRange = 1;

    protected final Action buildAction = new Action(this::buildMove);
    public void buildMove(GameMap gameMap, ArrayList<Action> actions) {
        pathing.moveOneStep(gameMap);
        if (pathing.hasNextStep()) {
            actions.add(currentAction);
        } else {
            if (owner.takeMoneyIfCan(450)) {
                new House(owner).build(gameMap, actions, buildBlockPos);
            }
        }
    }

    @Nullable
    private Point buildBlockPos = null;
    @TargetAbilityMethod(name = "build House")
    public void buildHouse(GameMap gameMap, ArrayList<Action> actions, Point target) throws Exception {
        Point buildBlockPos = gameMap.getBlockPosByTilePos(target);
        if (!gameMap.isBlockPosValid(buildBlockPos)) throw new Exception("invalid position");
        if (gameMap.getBlockByPos(buildBlockPos).getStructure() != null) throw new Exception("block already occupied");
        Point tileTarget = gameMap.getBlocksClosestTilePos(buildBlockPos, getTilePosition());
        if (gameMap.getDistance(getTilePosition(), tileTarget) > buildRange) {
            this.buildBlockPos = buildBlockPos;
            currentAction = buildAction;
            if (!pathing.hasNextStep()) {
                pathing.createPath(gameMap, getTilePosition(), tileTarget);
                actions.add(currentAction);
            } else {
                pathing.createPath(gameMap, getTilePosition(), tileTarget);
            }
        } else {
            owner.takeMoney(450);
            new House(owner).build(gameMap, actions, buildBlockPos);
        }
    }

    private Point mineTarget = null;
    private static final int mineRange = 1;

    private final Action mineAction = new Action(this::mineActionF);
    private Instant timeMineStart = null;

    private final Duration mineInterval = Duration.ofSeconds(5);
    private void mineActionF(GameMap gameMap, ArrayList<Action> actions) {
        if (gameMap.getDistance(getTilePosition(), mineTarget) > mineRange) {
            pathing.moveOneStep(gameMap);
        } else {
            if (timeMineStart == null) timeMineStart = Instant.now();
            if (Duration.between(timeMineStart, Instant.now()).compareTo(mineInterval) > 0) {
                timeMineStart = Instant.now();
                owner.addMoney(20);
            }
        }
        actions.add(currentAction);
    }

    @TargetAbilityMethod(name = "mine money")
    public void mineGold(GameMap gameMap, ArrayList<Action> actions, Point target) throws Exception {
        if (!gameMap.isTilePosValid(target)) throw new Exception("invalid position");
        if (!(gameMap.getTile(target) instanceof GoldMineTile)) throw new Exception("Invalid tile");
        mineTarget = target;
        currentAction = mineAction;
        if (!pathing.hasNextStep()) {
            actions.add(currentAction);
            pathing.createPath(gameMap, getTilePosition(), mineTarget);
        } else {
            pathing.createPath(gameMap, getTilePosition(), mineTarget);
        }
    }

}
