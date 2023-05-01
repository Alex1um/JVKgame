package GameMap.Tiles;

import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import GameMap.Units.Unit;
import org.joml.Vector4f;
import org.joml.Vector2f;

public abstract class Tile {
    Matrix4f color;

    protected Tile(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public GameMapVertex getVertex(int x, int y) {
        return vertexes[y][x];
    }

     GameMapVertex[][] vertexes = new GameMapVertex[2][2];

    public int getTextureIndex() {
        return textureIndex;
    }

    final int textureIndex;

    public int getVertexesIndex() {
        return vertexesIndex;
    }

    int vertexesIndex;
    public void setVertexesIndex(int vertexesIndex) {
        this.vertexesIndex = vertexesIndex;
    }

    @Nullable
    Unit unit = null;

    public Vector4f getVertexColor(int col, Vector4f out) {
        return color.getColumn(col, out);
    }
    
    protected void initVertixes(int tileSizePX, int tileRelativeBlockX, int tileRelativeBlockY) {
        // y - row; x - col
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++){
                Vector4f tileColor = new Vector4f();
                getVertexColor(y * 2 + x, tileColor);
                vertexes[y][x] = new GameMapVertex(
                        new Vector2f(
                                (tileRelativeBlockX * tileSizePX + x * tileSizePX),
                                (tileRelativeBlockY * tileSizePX + y * tileSizePX)
                                ),
                        tileColor,
                        new Vector2f(x, y),
                        textureIndex,
                        0
                );
            }
        }
    }

}
