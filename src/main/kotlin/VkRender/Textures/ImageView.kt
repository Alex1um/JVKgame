package VkRender.Textures

import VkRender.Device
import VkRender.TextureImage
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkImageViewCreateInfo
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class ImageView(private val ldevice: Device, stack: MemoryStack, image: Long, format: Int = VK_FORMAT_R8G8B8A8_SRGB) : Closeable {

    val view: Long
    init {
        val viewInfo = VkImageViewCreateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
            .image(image)
            .viewType(VK_IMAGE_TYPE_2D)
            .format(format)
            .subresourceRange {
                it
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1)
            }
//                .components {
//                    it.a(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
//                    it.r(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
//                    it.g(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
//                    it.b(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
//                }

        if (vkCreateImageView(ldevice.device, viewInfo, null, Util.lp) != VK_SUCCESS) {
            throw IllegalStateException("Cannot create image view")
        }

        view = Util.lp[0]
    }

    constructor(ldevice: Device, stack: MemoryStack, image: TextureImage, format: Int = VK_FORMAT_R8G8B8A8_SRGB) : this(ldevice, stack, image.image.imageh, format)
    constructor(ldevice: Device, stack: MemoryStack, image: Image, format: Int = VK_FORMAT_R8G8B8A8_SRGB) : this(ldevice, stack, image.imageh, format)

    override fun close() = vkDestroyImageView(ldevice.device, view, null)
}