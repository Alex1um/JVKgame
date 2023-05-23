package VkRender

object TextureTable {

    private const val imagesPath = "build/resources/main/images/"
    private const val imagesExt = ".png"

    val units = listOf(
        "worker",
        "knight",
        "mage",
        "orc",
        "necromancer",
        "zombie"
    ).withIndex().associateBy ({ it.value }, { it.index })

    val structures = listOf(
        "house",
        "houseConstructing",
        "temple",
        "templeConstructing"
    ).withIndex().associateBy ({ it.value }, { it.index + units.size })

    val tiles = arrayOf(
        "water",
        "grass",
        "goldMine"
    ).withIndex().associateBy ({ it.value }, { it.index })

    val objects = units + structures
    fun Map<String, Int>.getPaths(): Array<String> {
        return this.map {
            imagesPath + it.key + imagesExt
        }.toTypedArray()
    }
}