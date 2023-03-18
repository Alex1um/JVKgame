package VkRender.Textures

import VkRender.Config
import VkRender.Device
import VkRender.PhysicalDevice
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VkSamplerCreateInfo
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class Sampler(val ldevice: Device, physicalDevice: PhysicalDevice) : Closeable {

    val sampler: Long
    init {
        MemoryStack.stackPush().use { stack ->
            val samplerInfo = VkSamplerCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                .magFilter(VK_FILTER_LINEAR)
                .minFilter(VK_FILTER_LINEAR)
                .addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .anisotropyEnable(Config.ENABLE_ANISOTROPY)
                .maxAnisotropy(if (Config.ENABLE_ANISOTROPY) {1f} else {physicalDevice.properties.limits().maxSamplerAnisotropy()})
                .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                .unnormalizedCoordinates(false)
                .compareEnable(false)
                .compareOp(VK_COMPARE_OP_ALWAYS)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
                .mipLodBias(0f)
                .minLod(0f)
                .maxLod(0f)

            if (vkCreateSampler(ldevice.device, samplerInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Failed to create texture sampler")
            }
            sampler = Util.lp[0]
        }
    }

    override fun close() {
        vkDestroySampler(ldevice.device, sampler, null)
    }
}