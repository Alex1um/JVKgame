package GameMap.Tiles;

import VkRender.GPUObjects.GameMapVertex;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import GameMap.GameObjects.Units.Unit;
import org.joml.Vector4f;
import org.joml.Vector2f;

public abstract class Tile {

    public abstract float getMSFactor();
    protected Tile() {}
    @Nullable
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(@Nullable Unit unit) {
        this.unit = unit;
    }
    @Nullable
    Unit unit = null;

}
