package GameMap.Tiles;

import org.joml.Matrix4f;
import org.joml.Random;

public class GrassTile extends Tile {
    public GrassTile() {}

    @Override
    public float getMSFactor() {
        return 1f;
    }
}
