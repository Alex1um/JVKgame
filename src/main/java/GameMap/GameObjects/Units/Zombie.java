package GameMap.GameObjects.Units;

import java.time.Duration;

public class Zombie extends Unit {
    public Zombie() {
        super(500, new UnitStats(.25f, 1, 1, Duration.ofSeconds(1), 3));
    }
}
