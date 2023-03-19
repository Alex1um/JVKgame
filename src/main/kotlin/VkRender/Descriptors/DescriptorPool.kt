package VkRender.Descriptors

import VkRender.Config
import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import java.io.Closeable
class DescriptorPool(val ldevice: Device) : Closeable {

    val descriptorPool: Long

    init {
        MemoryStack.stackPush().use { stack ->
            val poolSize = VkDescriptorPoolSize.calloc(2, stack)
            poolSize[0]
                .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(Config.MAX_FRAMES_IN_FLIGHT)
            poolSize[1]
                .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(Config.MAX_FRAMES_IN_FLIGHT)

            val poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSize)
                .maxSets(Config.MAX_FRAMES_IN_FLIGHT)

            if (vkCreateDescriptorPool(ldevice.device, poolInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Cannot create descriptor pool")
            }

            descriptorPool = Util.lp[0]
        }
    }
    override fun close() {
        vkDestroyDescriptorPool(ldevice.device, descriptorPool, null)
    }
}