package VkRender.Descriptors

import VkRender.Config
import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import java.io.Closeable

class FilledDescriptorSetLayout(val ldevice: Device, val descriptorSetLayout: Long, val bindings: List<DescriptorSetLayout.Binding>) : Closeable {

    fun getPool() : DescriptorPool {

        MemoryStack.stackPush().use { stack ->
            val poolSize = VkDescriptorPoolSize.calloc(bindings.size, stack)
            for ((i, binding) in bindings.withIndex()) {
                poolSize[i]
                    .type(binding.type)
                    .descriptorCount(Config.MAX_FRAMES_IN_FLIGHT)
            }

            val poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSize)
                .maxSets(Config.MAX_FRAMES_IN_FLIGHT)

            if (VK13.vkCreateDescriptorPool(ldevice.device, poolInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("Cannot create descriptor pool")
            }

            return DescriptorPool(ldevice, Util.lp[0])
        }
    }

    override fun close() {
        VK13.vkDestroyDescriptorSetLayout(ldevice.device, descriptorSetLayout, null)
    }

}