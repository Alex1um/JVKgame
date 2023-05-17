package GameMap.GameObjects.Units;

import java.time.Duration;

public class UnitStats {
    float speedTilesPerFrame;
    float attack;
    int attackRange;
    Duration attackInterval;

    int attackScanRange;


    public UnitStats(
            float speedTilesPerFrame,
            float attack,
            int attackRange,
            Duration attackInterval,
            int attackScanRange
    ) {
        this.speedTilesPerFrame = speedTilesPerFrame;
        this.attack = attack;
        this.attackRange = attackRange;
        this.attackInterval = attackInterval;
        this.attackScanRange = attackScanRange;
    }
}
