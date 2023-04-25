package VkRender

import VkRender.GPUObjects.Properties
import VkRender.GPUObjects.GPUObject
import org.joml.Vector2fc
import org.joml.Vector4fc
import org.lwjgl.vulkan.VK13
import java.nio.ByteBuffer

class Vertex(
    var pos: Vector2fc,
    var color: Vector4fc,
    var texCoord: Vector2fc,
    var textureIndex: Int,
) : GPUObject() {

    companion object {

        val properties = Properties(
            (2 + 4 + 2) * Float.SIZE_BYTES + Int.SIZE_BYTES,
            arrayOf(
                0,
                2 * Float.SIZE_BYTES,
                (2 + 4) * Float.SIZE_BYTES,
                (2 + 4 + 2) * Float.SIZE_BYTES,
            ),
            arrayOf(
                VK13.VK_FORMAT_R32G32_SFLOAT,
                VK13.VK_FORMAT_R32G32B32A32_SFLOAT,
                VK13.VK_FORMAT_R32G32_SFLOAT,
                VK13.VK_FORMAT_R32_SINT,
            )
        )
    }
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
