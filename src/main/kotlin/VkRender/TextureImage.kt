package VkRender

import VkRender.Textures.Image
import VkRender.Textures.ImageView
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class TextureImage(val ldevice: Device, physicalDevice: PhysicalDevice, commands: CommandPool) : Closeable {

    val size: Int
    val width: Int
    val height: Int
    val image: Image
    val view: ImageView

    init {
        MemoryStack.stackPush().use { stack ->
            val pwidth = stack.ints(0)
            val pheight = stack.ints(0)
            val pchannels = stack.ints(0)
            val pixels: ByteBuffer = stbi_load("build/resources/main/images/2.png", pwidth, pheight, pchannels, STBI_rgb_alpha)
                ?: throw IllegalStateException("Cannot load image")
            width = pwidth[0]
            height = pheight[0]

            size = pwidth[0] * pheight[0] * /*always 4 due to stbi_rgb_alpha*/ pchannels[0]

            val stagingBuffer = VkRender.buffers.Buffer(
                ldevice, physicalDevice, size.toLong(), VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_COHERENT_BIT or VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
            )

            stagingBuffer.fill(pixels, size)

            stbi_image_free(pixels)

            image = Image(
                stack,
                ldevice,
                physicalDevice,
                width,
                height,
                VK_FORMAT_R8G8B8A8_SRGB,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT or VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
            )

            image.transitionImageLayout(commands, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
            // copyBuffer:
            stagingBuffer.copyToImage(stack, ldevice, commands, width, height, image)
            image.transitionImageLayout(commands, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)

            stagingBuffer.close()

            view = ImageView(ldevice, stack, image, VK_FORMAT_R8G8B8A8_SRGB)
        }
    }

    override fun close() {
        view.close()
        image.close()
    }
}