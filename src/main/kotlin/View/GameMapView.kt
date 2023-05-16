package View

import GameMap.GameMap
import GameMap.Tiles.GrassTile
import GameMap.Tiles.Tile
import VkRender.Config
import VkRender.GPUObjects.GameMapVertex
import VkRender.TextureTable
import org.joml.Vector2f
import org.joml.Vector4f

class GameMapView(gameMap: GameMap) {

    inner class TileView(tile: Tile, tileSizePX: Int, tileX: Int, tileY: Int) {

        val textureIndex = when (tile) {
            is GrassTile -> TextureTable.tiles["grass"]!!
            else -> -1
        }

        var vertexes = Array(2) { y ->
            Array(2) { x ->
                GameMapVertex(
                    Vector2f(
                        (tileX * tileSizePX + x * tileSizePX).toFloat(),
                        (tileY * tileSizePX + y * tileSizePX).toFloat()
                    ),
                    Vector2f(x.toFloat(), y.toFloat()),
                    textureIndex,
                    0
                )
            }
        }

        fun highlight(mode: Int = 1) {
            for (y in 0..1) {
                for (x in 0..1) {
                    vertexes[y][x]!!.isHighlighted = mode
                }
            }
        }

        fun unhighlight() {
            for (y in 0..1) {
                for (x in 0..1) {
                    vertexes[y][x]!!.isHighlighted = 0
                }
            }
        }
    }

    val tileViews = Array(gameMap.fullTileSize) { y ->
        Array(gameMap.fullTileSize) { x ->
            TileView(gameMap.getTile(x, y)!!, Config.tileSize, x, y)
        }
    }

    val vertices = tileViews.flatMap { it ->
            it.flatMap { view ->
                view.vertexes.flatten()
            }
        }
}