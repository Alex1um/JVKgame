package GameMap.Tiles;

import org.joml.Matrix4f;
import org.joml.Random;

public class GrassTile extends Tile {
    public GrassTile(int tileSizePX, int tileGlobalX, int tileGlobalY) {
        super(0);
        color = new Matrix4f(
                0.1f, 0.8f, 0.2f, 1.0f,
                0.5f, 0.9f, 0.0f, 1.0f,
                0.4f, 0.1f, 0.2f, 1.0f,
                0.1f, 0.5f, 0.0f, 1.0f);
        initVertixes(tileSizePX, tileGlobalX, tileGlobalY);
    }

    public GrassTile(Random r, int tileSizePX, int tileGlobalX, int tileGlobalY) {
        super(0);
        Matrix4f rng = new Matrix4f(
                r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat()
        );
        color = new Matrix4f(
                0.15f, 0.35f, 0.1f, 1.0f,
                0.15f, 0.35f, 0.1f, 1.0f,
                0.15f, 0.35f, 0.1f, 1.0f,
                0.15f, 0.35f, 0.1f, 1.0f).mul(rng);
        initVertixes(tileSizePX, tileGlobalX, tileGlobalY);
    }
}
