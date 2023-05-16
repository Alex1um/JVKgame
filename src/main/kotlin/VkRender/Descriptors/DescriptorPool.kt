package VkRender.Descriptors

import VkRender.Device
import org.lwjgl.vulkan.VK13.vkDestroyDescriptorPool
import java.io.Closeable

class DescriptorPool(val ldevice: Device, val descriptorPool: Long) : Closeable {

    override fun close() {
        vkDestroyDescriptorPool(ldevice.device, descriptorPool, null)
    }
}