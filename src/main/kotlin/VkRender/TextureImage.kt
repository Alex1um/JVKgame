package VkRender

import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class TextureImage {

    val size: Int

    init {
        MemoryStack.stackPush().use { stack ->
            val pwidth = stack.ints(0)
            val pheight = stack.ints(0)
            val pchannels = stack.ints(0)
            val pixels: ByteBuffer = stbi_load("build/resources/main/1.jpg", pwidth, pheight, pchannels, STBI_rgb_alpha) ?: throw IllegalStateException("Cannot load image")

            size = pwidth[0] * pheight[0] * /*always 4 due to stbi_rgb_alpha*/ pchannels[0]
        }
    }
}