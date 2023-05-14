package GameMap.Tiles;

import GameMap.GameObjects.Structures.Structure;
import VkRender.GPUObjects.GameMapVertex;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class StructureTile extends Tile {
    Structure structure;
    public StructureTile(int tileSizePX, int blockSize, Structure structure, int tileInBlockX, int tileInBlockY) {
        super(structure.textureIndex);
        this.structure = structure;
        color = new Matrix4f(
                .3f, .3f, .3f, 1f,
                .3f, .3f, .3f, 1f,
                .3f, .3f, .3f, 1f,
                .3f, .3f, .3f, 1f
        );
        initVertixes(tileSizePX, blockSize, tileInBlockX, tileInBlockY);
    }

    protected void initVertixes(int tileSizePX, int blockSize, int tileInBlockX, int tileInBlockY) {
        // y - row; x - col
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++){
                vertexes[y][x] = new GameMapVertex(
                        new Vector2f(
                                0f,
                                0f
                        ),
                        new Vector2f(
                                (float)(tileInBlockX * tileSizePX + x * tileSizePX) / (float)blockSize,
                                (float)(tileInBlockY * tileSizePX + y * tileSizePX) / (float)blockSize
                        ),
                        textureIndex,
                        0
                );
            }
        }
    }

    @Override
    public float getMSFactor() {
        return 0f;
    }
}
