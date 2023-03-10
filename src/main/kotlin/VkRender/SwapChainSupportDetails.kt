package VkRender

import VkRender.Surfaces.Surface
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.VK_FORMAT_B8G8R8A8_SRGB
import java.nio.IntBuffer

class SwapChainSupportDetails(stack: MemoryStack, physicalDevice: VkPhysicalDevice, sfc: Surface) {
    var capabilities: VkSurfaceCapabilitiesKHR
    var formats: VkSurfaceFormatKHR.Buffer
    var presentModes: IntBuffer

    init {
        with (Util) {
            with(sfc) {
                capabilities = VkSurfaceCapabilitiesKHR.malloc(stack)
                KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, capabilities)

                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, ip, null)
                presentModes = stack.mallocInt(ip[0])
                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, ip, presentModes)

                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, ip, null)
                formats = VkSurfaceFormatKHR.malloc(ip[0], stack)
                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, ip, formats)
            }
        }
    }

    fun chooseSwapSurfaceFormat(): VkSurfaceFormatKHR {
        for (i in 0 until formats.capacity()) {
            if (formats[i].format() == VK_FORMAT_B8G8R8A8_SRGB && formats.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return formats[i]
            }
        }
        return formats[0]
    }

    fun chooseSwapPresentMode(): Int {
        for (i in 0 until presentModes.capacity()) {
            if (presentModes[i] == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
                return KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR
            }
        }
        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR
    }

    fun chooseSwapExtent(): VkExtent2D {
        if (capabilities.currentExtent().width() != Int.MAX_VALUE) {
            return capabilities.currentExtent()
        } else {
            // TODO: do something
            throw IllegalStateException("Your screen is bad")
//            glfwGetFramebufferSize(window, )
        }
    }
}
