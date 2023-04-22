package GameMap.Blocks.Structures;

import GameMap.Blocks.Block;
import GameMap.Tiles.StructureTile;
import GameMap.Tiles.Tile;
import VkRender.Util;

public abstract class Structure {

    public final int textureIndex;

    protected Structure(int textureIndex) {
        this.textureIndex = textureIndex;
    }

}
