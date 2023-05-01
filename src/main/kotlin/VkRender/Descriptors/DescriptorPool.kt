package VkRender.Descriptors

import VkRender.Config
import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import java.io.Closeable
class DescriptorPool(val ldevice: Device, val descriptorPool: Long) : Closeable {

    override fun close() {
        vkDestroyDescriptorPool(ldevice.device, descriptorPool, null)
    }
}