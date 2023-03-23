package VkRender.GPUObjects

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkVertexInputAttributeDescription
import org.lwjgl.vulkan.VkVertexInputBindingDescription

class Properties (
    val SIZEOF: Int,
    val OFFSETS: Array<Int>,
    val FORMATS: Array<Int>,
) {

    fun getBindingDescription(stack: MemoryStack, binding: Int): VkVertexInputBindingDescription.Buffer {
        return VkVertexInputBindingDescription.calloc(1, stack)
            .binding(binding)
            .stride(SIZEOF)
            .inputRate(VK13.VK_VERTEX_INPUT_RATE_VERTEX)
    }

    fun getAttributeDescriptions(stack: MemoryStack, binding: Int): VkVertexInputAttributeDescription.Buffer {

        val descriptions = VkVertexInputAttributeDescription.calloc(OFFSETS.size, stack)
        for (i in OFFSETS.indices) {
            descriptions[i]
                .binding(binding)
                .location(i)
                .offset(OFFSETS[i])
                .format(FORMATS[i])
        }

        return descriptions
    }
}