package game

import game.materials.Material
import game.terrain_units.TerrainUnit
import game.terrain_units.WaterUnit

class Terrain(val width: Int, val height: Int) {

    val terrain = Array(height) { Array<TerrainUnit>(width) {
        WaterUnit(0)
    } }

}