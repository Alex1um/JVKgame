package VkRender.Sync

import VkRender.Device
import org.lwjgl.system.MemoryStack
import java.io.Closeable

class Fences(size: Int, private val device: Device, stack: MemoryStack) : Closeable {

    val fences = Array(size) { Fence(device, stack) }

    operator fun get(index: Int) = fences[index].fence

    override fun close() {
        for (fence in fences) {
            fence.close()
        }
    }

}