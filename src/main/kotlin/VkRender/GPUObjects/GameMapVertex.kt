package VkRender.GPUObjects

import org.joml.Vector2f
import org.joml.Vector2fc
import java.nio.ByteBuffer

class GameMapVertex(
    var pos: Vector2fc,
    var texCoord: Vector2fc,
    var textureIndex: Int,
    var isHighlighted: Int = 0,
) : GPUObject() {

    companion object : GPUObjectAutoProperties(Vector2f(), Vector2f(), Int, Int)

    override fun put(buffer: ByteBuffer) {

        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(texCoord.x())
        buffer.putFloat(texCoord.y())

        buffer.putInt(textureIndex)

        buffer.putInt(isHighlighted)
    }

}
