package Controller.Players;

import GameMap.GameObjects.Structures.Structure;
import GameMap.GameObjects.Units.Unit;

import java.util.ArrayList;

abstract public class Player {


    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<Structure> getStructures() {
        return structures;
    }

    ArrayList<Unit> units = new ArrayList<>();
    ArrayList<Structure> structures = new ArrayList<>();

}
