package GameMap.GameObjects.Units;

import Controller.Players.Player;

import java.time.Duration;

public class Zombie extends Unit {
    public Zombie(Player player) {
        super(player, 100, new UnitStats(.25f, 1, 1, Duration.ofSeconds(1), 3));
    }
}
