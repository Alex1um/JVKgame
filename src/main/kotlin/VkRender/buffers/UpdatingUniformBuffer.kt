package VkRender.buffers

import VkRender.Device
import VkRender.PhysicalDevice
import VkRender.Util
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class UpdatingUniformBuffer(val ldevice: Device, physicalDevice: PhysicalDevice, MAX_FRAMES_IN_FLIGHT: Int, val el_size: Long) : Closeable {

    val buffers: MutableList<Buffer>
    val mapped: PointerBuffer = MemoryUtil.memCallocPointer(MAX_FRAMES_IN_FLIGHT)

    init {
        MemoryStack.stackPush().use { stack ->
            buffers = mutableListOf()
            for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
                val buffer = Buffer(ldevice, physicalDevice, el_size, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)

                buffers.add(buffer)
                vkMapMemory(ldevice.device, buffer.vertexBufferMemory, 0, el_size, 0, Util.pp)
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

    fun update(currentFrame: Int, vararg floats: Float) {
        val bb = mapped.getByteBuffer(currentFrame, el_size.toInt())
        for (e in floats) {
            bb.putFloat(e)
        }

    }

}