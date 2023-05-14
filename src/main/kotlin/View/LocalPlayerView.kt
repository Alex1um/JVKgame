package View

import GameMap.Blocks.Block
import GameMap.GameMap
import GameMap.Tiles.Tile
import UI.VkFrame
import VkRender.Config
import VkRender.GPUObjects.GameMapVertex
import VkRender.GPUObjects.HealthBarVertex
import java.awt.Point

class LocalPlayerView internal constructor(
    val gameMap: GameMap,
    cameraInitPoint: Point,
) {

    var UI: VkFrame? = null;

    var vkUI = VkUI()
    companion object Consts {
        private const val MAX_SCALE = 0.05f
        private const val MIN_SCALE = 0.0005f
        // Скорость перемещения и масштабирования
        private const val MOVE_SPEED = 50.0f
        private const val SCALE_SPEED = 0.0001f
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

    val mapIndexes = mutableListOf(gameMap.fullTileSize * gameMap.fullTileSize)
        get;

    val mapVertices: MutableList<GameMapVertex> = mutableListOf();

    val camera = Camera(cameraInitPoint.x.toFloat(), cameraInitPoint.y.toFloat())

    val gameMapView = GameMapView(gameMap)

    init {
//        generateMapVertices(gameMap)
        generateMapIndexes(gameMap)
    }

    private fun generateMapIndexes(map: GameMap): MutableList<Int> {
        mapIndexes.clear()
        var tileIndex = 0
        for (blockY in 0 until map.mapSizeBlocks) {
            for (blockX in 0 until map.mapSizeBlocks) {
                for (tileY in 0 until map.blockSizeTiles) {
                    for (tileX in 0 until map.blockSizeTiles) {
                        mapIndexes.add(tileIndex)
                        mapIndexes.add(tileIndex + 1)
                        mapIndexes.add(tileIndex + 3)
                        mapIndexes.add(tileIndex + 3)
                        mapIndexes.add(tileIndex + 2)
                        mapIndexes.add(tileIndex)
                        tileIndex += 4;
                    }
                }
            }
        }
        return mapIndexes
    }

    fun getTilePositionByClick(clickPos: Point): Point {
        val canvasWidthHalved = (UI!!.canvas.width / 2).toFloat()
        val canvasHeightHalved = (UI!!.canvas.height / 2).toFloat()
        val mousePosX: Float = (clickPos.x
            .toFloat() - canvasWidthHalved - camera.offsetX * canvasWidthHalved) / (canvasWidthHalved * camera.scale) / Config.tileSize.toFloat()
        val mousePosY: Float = (clickPos.y
            .toFloat() - canvasHeightHalved - camera.offsetY * canvasHeightHalved) / (canvasHeightHalved * camera.scale) / Config.tileSize.toFloat()
        return Point(mousePosX.toInt(), mousePosY.toInt());
    }
    fun getTileByMouseClick(clickPos: Point): Tile? {
        val mousePos = getTilePositionByClick(clickPos)
        return gameMap.getTile(mousePos)
    }

    fun getBlockByMouseClick(clickPos: Point): Block? {
        val mousePos = getTilePositionByClick(clickPos)
        return gameMap.getBlockByTilePos(mousePos)
    }

    fun getBlockPositionByClick(clickPos: Point): Point {
        val mousePos = getTilePositionByClick(clickPos)
        return gameMap.getBlockPosByTilePos(mousePos)
    }

    var mapObjects: List<GameMapVertex> = listOf()
        private set
        get() {
            if (field.size != gameMap.objects.size * 4) {
                field = gameMap.objects.flatMap { it.vertexes.flatten() }
            }
            return field
        }

    fun getMapObjectsIndexCount(): Int {
        return gameMap.objects.size * 6
    }

    var healthBars: List<HealthBarVertex> = listOf()
        private set
        get() {
            if (field.size != gameMap.objects.size * 4) {
                field = gameMap.objects.flatMap { it.healthBar.flatten() }
            }
            return field
        }

}
