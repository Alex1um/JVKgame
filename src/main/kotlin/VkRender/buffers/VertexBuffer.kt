package VkRender.buffers

import VkRender.*
import VkRender.GPUObjects.GPUObject
import VkRender.GPUObjects.GameMapVertex
import VkRender.GPUObjects.Properties
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable
import java.nio.ByteBuffer

class VertexBuffer(val ldevice: Device, physicalDevice: PhysicalDevice,
                   private var len: Int, val vertexProps: Properties) : Closeable {

    val buffer: Buffer
    private val mapped = MemoryUtil.memAllocPointer(1)
    private val size: Int
        get() = vertexProps.SIZEOF * len

    init {

        buffer = Buffer(ldevice, physicalDevice, size.toLong(), VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )

        vkMapMemory(ldevice.device, buffer.vertexBufferMemory, 0, size.toLong(), 0, mapped)
    }

    private fun getByteBuffer(): ByteBuffer {
        return mapped.getByteBuffer(0, vertexProps.SIZEOF * len)
    }

    fun resize(new_len: Int) {
        vkUnmapMemory(ldevice.device, buffer.vertexBufferMemory)
        len = new_len
        vkMapMemory(ldevice.device, buffer.vertexBufferMemory, 0, size.toLong(), 0, mapped)
    }

    fun update(arr: List<GPUObject>) {
        val byteBuffer = getByteBuffer()
        for (e in arr) {
            e.put(byteBuffer)
        }
    }

    override fun close() {
        vkUnmapMemory(ldevice.device, buffer.vertexBufferMemory)
        buffer.close()
        mapped.free()
    }

}