package VkRender.buffers

import VkRender.CommandPool
import VkRender.Device
import VkRender.PhysicalDevice
import VkRender.GPUObjects.GameMapVertex
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class VertexStagingBuffer(ldevice: Device, physicalDevice: PhysicalDevice, vertices: List<GameMapVertex>, commands: CommandPool) : Closeable {

    val buffer: Buffer

    init {
        val bufferSize = (vertices.size * GameMapVertex.properties.SIZEOF).toLong()
        val stagingBuffer = Buffer(
            ldevice,
            physicalDevice,
            bufferSize,
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT or VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)

        stagingBuffer.fillVertices(vertices)

        buffer = Buffer(
            ldevice,
            physicalDevice,
            bufferSize,
            VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

        stagingBuffer.copyBuffer(ldevice, commands, buffer)

        stagingBuffer.close()
    }

    override fun close() {
//        stagingBuffer.close()
        buffer.close()
    }
}