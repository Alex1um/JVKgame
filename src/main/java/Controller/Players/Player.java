package Controller.Players;

import GameMap.GameObjects.Structures.Structure;
import GameMap.GameObjects.Units.Unit;

import java.util.ArrayList;

abstract public class Player {

    public int getMoney() {
        return money;
    }

    public void takeMoney(int count) throws Exception {
        if (count > money) throw new Exception("Not enough money");
        money -= count;
    }

    public boolean takeMoneyIfCan(int count) {
        if (count > money) return false;
        money -= count;
        return true;
    }

    public void addMoney(int count) {
        money += count;
    }

    int money = 500;

    public int getGroup() {
        return group;
    }

    int group;
    private static int globalGroup = 0;

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Structure> getStructures() {
        return structures;
    }

    ArrayList<Unit> units = new ArrayList<>();
    ArrayList<Structure> structures = new ArrayList<>();

    public Player() {
        group = globalGroup;
        globalGroup += 1;
    }

    public Player(int group) {
        this.group = group;
    }

}
