package VkRender.Sync

import VkRender.Device
import org.lwjgl.system.MemoryStack
import java.io.Closeable

class Semaphores(size: Int, private val device: Device, stack: MemoryStack) : Closeable {

    val semaphores = Array(size) {Semaphore(device, stack)}

    operator fun get(index: Int) = semaphores[index].semaphore

    override fun close() {
        for (semaphore in semaphores) {
            semaphore.close()
        }
    }
}