package VkRender.buffers

import VkRender.Device
import VkRender.PhysicalDevice
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class SquareSizeBuffer(val ldevice: Device, physicalDevice: PhysicalDevice, MAX_FRAMES_IN_FLIGHT: Int, val size: Long) : Closeable {

    val buffers: MutableList<Buffer>
    val mapped = MemoryUtil.memCallocPointer(MAX_FRAMES_IN_FLIGHT)

    init {
        MemoryStack.stackPush().use { stack ->
            buffers = mutableListOf()
            for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
                val buffer = Buffer(ldevice, physicalDevice, size, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)

                buffers.add(buffer)
                vkMapMemory(ldevice.device, buffer.vertexBufferMemory, 0, size, 0, Util.pp)
                mapped.put(Util.pp[0])
            }
        }
    }

    override fun close() {
        for ((i, buffer) in buffers.withIndex()) {
            vkUnmapMemory(ldevice.device, mapped[i])
            buffer.close()
        }
        mapped.free()
    }

    fun update(currentFrame: Int, width: Float, height: Float) {
        val bb = mapped.getByteBuffer(currentFrame, size.toInt())
        bb.putFloat(width)
        bb.putFloat(height)
    }

}