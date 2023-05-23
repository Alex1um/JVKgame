package GameMap.Tiles;

public class WaterTile extends Tile {

    public WaterTile() {}

    @Override
    public float getMSFactor() {
        return 0.05f;
    }
}
