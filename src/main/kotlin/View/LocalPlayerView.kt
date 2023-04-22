package View

import GameMap.GameMap
import VkRender.Vertex
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import kotlin.math.ceil
import kotlin.math.floor

class LocalPlayerView internal constructor(
    view_init_center_tile_x: Int,
    view_init_center_tile_y: Int,
    val max_width_tiles: Int,
    val max_height_tiles: Int,
    val tileSizePX: Int
) {
    val camera_rect_tiles: Rectangle

    val indexes = mutableListOf(max_width_tiles * max_height_tiles)
        get;

    private val baseSize: Dimension = Dimension(
        floor(max_width_tiles * tileSizePX / 2f).toInt(),
        floor(max_height_tiles * tileSizePX / 2f).toInt()
    )

    init {
        camera_rect_tiles = Rectangle(
            view_init_center_tile_x - ceil(max_width_tiles / 2f).toInt(),
            view_init_center_tile_y - ceil(max_height_tiles / 2f).toInt(),
            baseSize.width,
            baseSize.height
        )
    }
    val vertices: MutableList<Vertex> = mutableListOf();

    fun setCameraCoords(x: Int, y: Int) {
        camera_rect_tiles.x = x;
        camera_rect_tiles.y = y
    }

    fun getCameraCoords(): Point {
        return Point(camera_rect_tiles.x, camera_rect_tiles.y)
    }

    private var scaleFactor: Double = 1.0;

    fun scale(upDown: Double) {
        if ((upDown < 0 && scaleFactor > 0.05) || (upDown > 0 && scaleFactor < 5)) {
            scaleFactor += 0.05 * upDown
        }
        val dx = baseSize.getWidth() * scaleFactor
        val dy = baseSize.getHeight() * scaleFactor
        camera_rect_tiles.setSize(
            dx.toInt(),
            dy.toInt(),
        )
    }

    fun generateVertices(map: GameMap): List<Vertex> {
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

    fun generateIndexes(map: GameMap): MutableList<Int> {
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

    var isCameraMoving = false;

    // Selection
    val selectionRect = Rectangle()

    var isCameraScaled = false
        get() {
            if (field) {
                field = false
                return true
            }
            return field
        }

    var cameraMovementStartingPoint: Point? = null

    var cameraStartingPoint: Point? = null

    var selectionStartingPoint: Point? = null

    fun isSelecting(): Boolean {
        return selectionStartingPoint != null
    }

}
