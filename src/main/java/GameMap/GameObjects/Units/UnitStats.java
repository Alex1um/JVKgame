package GameMap.GameObjects.Units;

public class UnitStats {
    Integer hpMax;
    Integer hp;
    Integer speedTilesPerFrame;

    public UnitStats(Integer hpMax, Integer speedTilesPerFrame) {
        this.hpMax = hpMax;
        this.hp = hpMax;
        this.speedTilesPerFrame = speedTilesPerFrame;
    }
}
