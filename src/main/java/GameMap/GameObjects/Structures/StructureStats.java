package GameMap.GameObjects.Structures;

public class StructureStats {
    Integer hpMax;
    Integer hp = hpMax;
    String name;

    public StructureStats(Integer hpMax, String name) {
        this.hpMax = hpMax;
        this.name = name;
    }
}
