package View

import GameMap.GameMap
import GameMap.GameObjects.GameObject
import GameMap.GameObjects.Structures.House
import GameMap.GameObjects.Structures.Structure
import GameMap.GameObjects.Structures.Temple
import GameMap.GameObjects.Units.Necromancer
import GameMap.GameObjects.Units.Zombie
import GameMap.GameObjects.Units.Unit
import VkRender.Config
import VkRender.GPUObjects.GameMapVertex
import VkRender.GPUObjects.HealthBarVertex
import VkRender.TextureTable
import org.joml.Vector2f

class GameObjectsView(private val gameMap: GameMap) {

    inner class GameObjectView(val gameObject: GameObject) {

        inner class HealthBarView(val gameObjectView: GameObjectView) {

            private var percent = gameObject.health / gameObject.maxHealth
            private var healthSplitX = gameObjectView.vertexes[0][0].pos.x() + Config.tileSize * percent

            val vertexes = arrayOf (
                arrayOf(
                    HealthBarVertex(
                        Vector2f(
                            gameObjectView.vertexes[0][0].pos.x(),
                            gameObjectView.vertexes[0][0].pos.y() - Config.healthBarHeight,
                        ),
                        healthSplitX,
                        percent
                    ),
                    HealthBarVertex(
                        Vector2f(
                            gameObjectView.vertexes[0][1].pos.x(),
                            gameObjectView.vertexes[0][1].pos.y() - Config.healthBarHeight,
                        ),
                        healthSplitX,
                        percent
                    ),
                ),
                arrayOf(
                    HealthBarVertex(
                        gameObjectView.vertexes[0][0].pos,
                        healthSplitX,
                        percent
                    ),
                    HealthBarVertex(
                        gameObjectView.vertexes[0][1].pos,
                        healthSplitX,
                        percent
                    ),
                )
            )

            fun update() {
                vertexes[0][0].pos = Vector2f(
                    gameObjectView.vertexes[0][0].pos.x(),
                    gameObjectView.vertexes[0][0].pos.y() - Config.healthBarHeight,
                )
                vertexes[0][1].pos = Vector2f(
                    gameObjectView.vertexes[0][1].pos.x(),
                    gameObjectView.vertexes[0][1].pos.y() - Config.healthBarHeight,
                )
                vertexes[1][0].pos = gameObjectView.vertexes[0][0].pos
                vertexes[1][1].pos = gameObjectView.vertexes[0][1].pos
                percent = gameObjectView.gameObject.health / gameObjectView.gameObject.maxHealth
                healthSplitX = gameObjectView.vertexes[0][0].pos.x() + Config.tileSize * percent
                vertexes.forEach { row ->
                    row.forEach { vertex ->
                        vertex.healthSplitX = healthSplitX
                        vertex.healthPercent = percent
                    }
                }
            }
        }

        val textureIndex = when (gameObject) {
            is Necromancer -> TextureTable.units["necromancer"]!!
            is Zombie -> TextureTable.units["zombie"]!!
            is Temple -> TextureTable.structures["temple"]!!
            is House -> TextureTable.structures[if (gameObject.isBuilt) "house" else "houseConstructing"]!!
            else -> -1
        }

        val vertexes: Array<Array<GameMapVertex>> = when (gameObject) {
            is Structure -> {
                Array(2) { y ->
                    Array(2) { x ->
                        GameMapVertex(
                            Vector2f(
                                ((gameObject.blockPosition.x + x) * Config.tileSize * gameMap.blockSizeTiles).toFloat(),
                                ((gameObject.blockPosition.y + y) * Config.tileSize * gameMap.blockSizeTiles).toFloat(),
                            ),
                            Vector2f(
                                x.toFloat(),
                                y.toFloat(),
                            ),
                            textureIndex,
                            0,
                        )
                    }
                }
            }
            is Unit -> {
                Array(2) { y ->
                    Array(2) { x ->
                        GameMapVertex(
                            Vector2f(
                                ((gameObject.tilePosition.x + x) * Config.tileSize).toFloat(),
                                ((gameObject.tilePosition.y + y) * Config.tileSize).toFloat(),
                            ),
                            Vector2f(
                                x.toFloat(),
                                y.toFloat(),
                            ),
                            textureIndex,
                            0,
                        )
                    }
                }
            }
            else -> {
                Array(2) { y ->
                    Array(2) { x ->
                        GameMapVertex(
                            Vector2f(
                                0f,
                                0f,
                            ),
                            Vector2f(
                                x.toFloat(),
                                y.toFloat(),
                            ),
                            textureIndex,
                            0,
                        )
                    }
                }
            }
        }

        val healthbarView = HealthBarView(this)

        fun update() {
            vertexes.forEachIndexed { y, gameMapVertices ->
                gameMapVertices.forEachIndexed { x, gameMapVertex ->
                    gameMapVertex.pos = when (gameObject) {
                        is Structure -> Vector2f(
                            ((gameObject.blockPosition.x + x) * Config.tileSize * gameMap.blockSizeTiles).toFloat(),
                            ((gameObject.blockPosition.y + y) * Config.tileSize * gameMap.blockSizeTiles).toFloat(),
                        )
                        is Unit -> Vector2f(
                            ((gameObject.tilePosition.x + x) * Config.tileSize).toFloat(),
                            ((gameObject.tilePosition.y + y) * Config.tileSize).toFloat(),
                        )
                        else -> Vector2f()
                    }
                }
            }
        }

        fun highlight(mode: Int = 0) {
            vertexes.forEach { vr -> vr.forEach { it.isHighlighted = mode } }
        }

    }

    var gameObjectsView = listOf<GameObjectView>()
        private set

    fun getObjectView(gameObject: GameObject): GameObjectView? {
        return gameObjectsView.find { it.gameObject == gameObject }
    }

    var vertexes = gameObjectsView.flatMap { view ->
        view.vertexes.flatten()
    }
        private set

    var vertexesHealthBar = gameObjectsView.flatMap {  view ->
        view.healthbarView.vertexes.flatten()
    }
        private set

    fun update() {
        if (gameObjectsView.size != gameMap.objects.size) {
            gameObjectsView = gameMap.objects.map { obj ->
                GameObjectView(obj)
            }
            this.vertexes = gameObjectsView.flatMap { view ->
                view.vertexes.flatten()
            }
            this.vertexesHealthBar = gameObjectsView.flatMap {  view ->
                view.healthbarView.vertexes.flatten()
            }
        } else {
            gameObjectsView.forEach { obj ->
                obj.update()
                obj.healthbarView.update()
            }
        }
    }

}