package View

import GameMap.GameMap
import VkRender.Vertex
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import kotlin.math.ceil
import kotlin.math.floor

class LocalPlayerView internal constructor(
    gameMap: GameMap,
    cameraInitPoint: Point,
    val tileSizePX: Int = 20,
) {

    companion object Consts {
        private const val MAX_SCALE = 0.05f
        private const val MIN_SCALE = 0.005f
        // Скорость перемещения и масштабирования
        private const val MOVE_SPEED = 50.0f
        private const val SCALE_SPEED = 0.001f
    }

    inner class Camera(
        var offsetX: Float = 0f,
        var offsetY: Float = 0f,
        var scale: Float = MIN_SCALE,
    ) {

        fun move(dx: Int, dy: Int) {
            offsetX += dx * scale
            offsetY += dy * scale
        }

        fun zoomIn() {
            scale += Consts.SCALE_SPEED
            if (scale > Consts.MAX_SCALE) {
                scale = Consts.MAX_SCALE
            }
        }

        fun zoomOut() {
            scale -= Consts.SCALE_SPEED
            if (scale < Consts.MIN_SCALE) {
                scale = Consts.MIN_SCALE
            }
        }

    }

    val indexes = mutableListOf(gameMap.fullTileSize * gameMap.fullTileSize)
        get;

    val vertices: MutableList<Vertex> = mutableListOf();

    val camera = Camera(cameraInitPoint.x.toFloat(), cameraInitPoint.y.toFloat())

    init {
        generateVertices(gameMap)
        generateIndexes(gameMap)
    }

    private fun generateVertices(map: GameMap): List<Vertex> {
        vertices.clear()
        var tileArrayIndex = 0;
        for (blockY in 0 until map.size) {
            for (blockX in 0 until map.size) {
                val block = map.getBlock(blockX, blockY);
                for (tileY in 0 until map.blockSize) {
                    for (tileX in 0 until map.blockSize) {
                        val tile = block.getTile(tileX, tileY);
                        tile.setVertexesIndex(tileArrayIndex)
                        tileArrayIndex += 1
                        for (y in 0 until 2) {
                            for (x in 0 until 2) {
//                                val tileColor = Vector4f()
//                                tile.getVertexColor(y * 2 + x, tileColor)
//                                vertices.add(
//                                    Vertex(
//                                        Vector2f(
//                                            ((blockX * map.blockSize + tileX) * tileSizePX + x * tileSizePX).toFloat(),
//                                            ((blockY * map.blockSize + tileY) * tileSizePX + y * tileSizePX).toFloat(),
//                                        ),
//                                        tileColor,
//                                        Vector2f(x.toFloat(), y.toFloat())
//                                    )
//                                )
                                vertices.add(tile.getVertex(x, y))
                            }
                        }
                    }
                }
            }
        }
        return vertices
    }

    private fun generateIndexes(map: GameMap): MutableList<Int> {
        indexes.clear()
        var tileIndex = 0
        for (blockY in 0 until map.size) {
            for (blockX in 0 until map.size) {
                for (tileY in 0 until map.blockSize) {
                    for (tileX in 0 until map.blockSize) {
                        indexes.add(tileIndex)
                        indexes.add(tileIndex + 1)
                        indexes.add(tileIndex + 3)
                        indexes.add(tileIndex + 3)
                        indexes.add(tileIndex + 2)
                        indexes.add(tileIndex)
                        tileIndex += 4;
                    }
                }
            }
        }
        return indexes
    }

}
