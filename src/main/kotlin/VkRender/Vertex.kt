package VkRender

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkVertexInputBindingDescription
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkVertexInputAttributeDescription
import java.nio.ByteBuffer

class Vertex(
    val pos: Vector2fc,
    val color: Vector3fc,
    val texCoord: Vector2fc,
) {

    companion object {

        val SIZEOF = (2 + 3 + 2) * Float.SIZE_BYTES
        private val OFFSET_POS = 0
        private val OFFSET_COLOR = 2 * Float.SIZE_BYTES
        private val OFFSET_TEX_COORD = (2 + 3) * Float.SIZE_BYTES

        fun getBindingDescription(stack: MemoryStack): VkVertexInputBindingDescription.Buffer {
            return VkVertexInputBindingDescription.calloc(1, stack)
                .binding(0)
                .stride(SIZEOF)
                .inputRate(VK13.VK_VERTEX_INPUT_RATE_VERTEX)
        }

        fun getAttributeDescriptions(stack: MemoryStack): VkVertexInputAttributeDescription.Buffer {
            val descriptions = VkVertexInputAttributeDescription.calloc(3, stack)
            descriptions[0]
                .binding(0)
                .location(0)
                .offset(OFFSET_POS)
                .format(VK13.VK_FORMAT_R32G32_SFLOAT)

            descriptions[1]
                .binding(0)
                .location(1)
                .offset(OFFSET_COLOR)
                .format(VK13.VK_FORMAT_R32G32B32_SFLOAT)

            descriptions[2]
                .binding(0)
                .location(2)
                .offset(OFFSET_TEX_COORD)
                .format(VK13.VK_FORMAT_R32G32_SFLOAT)

            return descriptions
        }
    }

    fun put(buffer: ByteBuffer) {
        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(color.x())
        buffer.putFloat(color.y())
        buffer.putFloat(color.z())

        buffer.putFloat(texCoord.x())
        buffer.putFloat(texCoord.y())
    }

}
