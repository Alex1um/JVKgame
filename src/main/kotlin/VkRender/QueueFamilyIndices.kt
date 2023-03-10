package VkRender

import VkRender.Surfaces.Surface
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkPhysicalDevice
import org.lwjgl.vulkan.VkQueueFamilyProperties

class QueueFamilyIndices {

    var graphicsFamily: Int? = null
    var presentFamily: Int? = null

    constructor(stack: MemoryStack, physicalDevice: VkPhysicalDevice, surface: Surface) {
        with (Util) {

            VK13.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, null)
            val tmp_queue_props = VkQueueFamilyProperties.calloc(ip[0], stack)
            VK13.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, tmp_queue_props)

            val supportsPresent = stack.mallocInt(tmp_queue_props.capacity())
            for (i in 0 until tmp_queue_props.capacity()) {
                supportsPresent.position(i)
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface.surface, supportsPresent)
                if (supportsPresent[i] == VK13.VK_TRUE) {
                    presentFamily = i
                    break
                }
            }
            for (i in 0 until tmp_queue_props.capacity()) {
                if (tmp_queue_props[i].queueFlags() and VK13.VK_QUEUE_GRAPHICS_BIT != 0) {
                    graphicsFamily = i
                    break
                }
            }
        }
    }

    constructor(stack: MemoryStack,physicalDevice: PhysicalDevice, surface: Surface) : this(stack, physicalDevice.physicalDevice, surface)

    fun isComplete(): Boolean {

        return graphicsFamily != null && presentFamily != null
    }
}