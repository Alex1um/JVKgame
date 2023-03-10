package VkRender.Sync

import VkRender.Device
import java.io.Closeable

class Fences(size: Int, private val device: Device) : Closeable {

    val fences = Array(size) { Fence(device) }

    operator fun get(index: Int) = fences[index].fence

    override fun close() {
        for (fence in fences) {
            fence.close()
        }
    }

}