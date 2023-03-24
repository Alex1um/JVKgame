package VkRender.GPUObjects
import java.nio.ByteBuffer

abstract class Vertex {
    abstract fun put(buffer: ByteBuffer)
}