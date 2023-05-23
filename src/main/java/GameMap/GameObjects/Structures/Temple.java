package GameMap.GameObjects.Structures;


import Controller.Players.Player;

public class Temple extends Structure {

    public Temple(Player player) {
        super(player, 500, new StructureStats(500, "Temple"));
    }



}
