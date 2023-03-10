package VkRender

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil
import java.io.Closeable
import java.io.File
import java.nio.IntBuffer
import java.nio.LongBuffer

class Util : Closeable {

//    val ip = MemoryUtil.memAllocInt(1)
//    val lp = MemoryUtil.memAllocLong(1)
//    val pp = MemoryUtil.memAllocPointer(1)

    init {
        ip = MemoryUtil.memAllocInt(1)
        lp = MemoryUtil.memAllocLong(1)
        pp = MemoryUtil.memAllocPointer(1)
        ptrBuf = MemoryUtil.memAllocPointer(64);
    }

    companion object {
        lateinit var ip: IntBuffer
        lateinit var lp: LongBuffer
        lateinit var pp: PointerBuffer
        lateinit var ptrBuf: PointerBuffer


        fun readFile(filename: String): ByteArray {
            with(File(filename)) {
                return this.readBytes()
            }
        }
    }

    override fun close() {
        MemoryUtil.memFree(pp)
        MemoryUtil.memFree(lp)
        MemoryUtil.memFree(ip)
        MemoryUtil.memFree(ptrBuf)
    }
}
