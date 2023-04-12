package Controller.Players;

import GameMap.Blocks.Structures.Structure;
import GameMap.Units.Unit;

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
