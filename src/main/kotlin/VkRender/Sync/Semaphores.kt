package VkRender.Sync

import VkRender.Device
import java.io.Closeable

class Semaphores(size: Int, private val device: Device) : Closeable {

    val semaphores = Array(size) {Semaphore(device)}

    operator fun get(index: Int) = semaphores[index].semaphore

    override fun close() {
        for (semaphore in semaphores) {
            semaphore.close()
        }
    }
}