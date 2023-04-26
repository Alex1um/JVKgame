package VkRender

import VkRender.GPUObjects.GPUObject
import VkRender.GPUObjects.GPUObjectProperties
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f
import org.joml.Vector4fc
import java.nio.ByteBuffer

class Vertex(
    var pos: Vector2fc,
    var color: Vector4fc,
    var texCoord: Vector2fc,
    var textureIndex: Int,
) : GPUObject() {

    companion object : GPUObjectProperties(Vector2f(), Vector4f(), Vector2f(), Int)

    override fun put(buffer: ByteBuffer) {

        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(color.x())
        buffer.putFloat(color.y())
        buffer.putFloat(color.z())
        buffer.putFloat(color.w())

        buffer.putFloat(texCoord.x())
        buffer.putFloat(texCoord.y())

        buffer.putInt(textureIndex)
    }

}
