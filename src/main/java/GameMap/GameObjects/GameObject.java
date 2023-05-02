package GameMap.GameObjects;

import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;

public abstract class GameObject {

    @Nullable
    Integer textureIndex = null;

    public GameMapVertex[][] getVertixes() {
        return vertixes;
    }

    GameMapVertex[][] vertixes = new GameMapVertex[2][2];

    protected GameMapVertex getVertex(int x, int y) {
        return vertixes[y][x];
    }
    protected void setVertex(int x, int y, GameMapVertex vertex) {
        vertixes[y][x] = vertex;
    }
}
