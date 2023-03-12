package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkQueue
import java.io.Closeable

class IndexBuffer(
    val ldevice: Device,
    physicalDevice: PhysicalDevice,
    indexes: Array<Int>,
    commandPool: CommandPool,
    graphicsQueue: VkQueue
) : Closeable {

    val length = indexes.size
    val size: Long = (Int.SIZE_BYTES * indexes.size).toLong()
    val buffer: Buffer

    init {
        MemoryStack.stackPush().use { stack ->
            val stagingBuffer = Buffer(ldevice, physicalDevice, size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)

            stagingBuffer.fill(indexes)

            buffer = Buffer(ldevice, physicalDevice, size, VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK_BUFFER_USAGE_INDEX_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

            stagingBuffer.copyBuffer(ldevice, commandPool, graphicsQueue, buffer)

            stagingBuffer.close()
        }
    }

    override fun close() {
        buffer.close()
    }
}