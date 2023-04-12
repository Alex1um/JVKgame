package GameMap.Tiles;

import VkRender.Vertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import GameMap.Units.Unit;
import org.joml.Vector4f;
import org.joml.Vector2f;

public abstract class Tile {
    Matrix4f color;

    public Vertex getVertex(int x, int y) {
        return vertexes[y][x];
    }

     Vertex[][] vertexes = new Vertex[2][2];
    int textureIndex;


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
                vertexes[y][x] = new Vertex(
                        new Vector2f(
                                (tileRelativeBlockX * tileSizePX + x * tileSizePX),
                                (tileRelativeBlockY * tileSizePX + y * tileSizePX)
                                ),
                        tileColor,
                        new Vector2f(x, y)
                );
            }
        }
    }

}
