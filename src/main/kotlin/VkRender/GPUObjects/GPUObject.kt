package VkRender.GPUObjects
import java.nio.ByteBuffer

abstract class GPUObject {
    abstract fun put(buffer: ByteBuffer)
}