package VkRender

import VkRender.GPUObjects.Properties
import VkRender.GPUObjects.Vertex
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.vulkan.VK13
import java.nio.ByteBuffer

class Vertex(
    val pos: Vector2fc,
    val color: Vector3fc,
    val texCoord: Vector2fc,
) : Vertex() {

    companion object {

        val properties = Properties(
            (2 + 3 + 2) * Float.SIZE_BYTES,
            arrayOf(
                0,
                2 * Float.SIZE_BYTES,
                (2 + 3) * Float.SIZE_BYTES,
            ),
            arrayOf(
                VK13.VK_FORMAT_R32G32_SFLOAT,
                VK13.VK_FORMAT_R32G32B32_SFLOAT,
                VK13.VK_FORMAT_R32G32_SFLOAT,
            )
        )
    }
    override fun put(buffer: ByteBuffer) {
        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(color.x())
        buffer.putFloat(color.y())
        buffer.putFloat(color.z())

        buffer.putFloat(texCoord.x())
        buffer.putFloat(texCoord.y())
    }

}
