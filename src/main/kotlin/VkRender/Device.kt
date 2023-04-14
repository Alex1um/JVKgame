package VkRender

import VkRender.Surfaces.Surface
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import java.io.Closeable

class Device(physicalDevice: PhysicalDevice): Closeable {

    val device: VkDevice
    val graphicsQueue: VkQueue
    val memoryProperties: VkPhysicalDeviceMemoryProperties
    val graphicsFamily: Int;
    val presentFamily: Int;

    init {
        with(Util) {
            MemoryStack.stackPush().use { stack ->

                if (physicalDevice.graphicsFamily != physicalDevice.presentFamily) {
                    throw IllegalStateException("families are not same. Not supported")
                }

                val queue = if (physicalDevice.graphicsFamily == physicalDevice.presentFamily) {
                    VkDeviceQueueCreateInfo.calloc(/*Queue count: */1, stack)
                        .sType(VK13.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(physicalDevice.graphicsFamily)
                        .pQueuePriorities(stack.floats(1.0f))
                } else {
                    val q = VkDeviceQueueCreateInfo.calloc(/*Queue count: */2, stack)
                    q
                        .sType(VK13.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(physicalDevice.graphicsFamily)
                        .pQueuePriorities(stack.floats(1.0f))
                    q
                        .sType(VK13.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(physicalDevice.presentFamily)
                        .pQueuePriorities(stack.floats(1.0f))
                    q
                }

                val features = VkPhysicalDeviceFeatures.calloc(stack)
                if (Config.ENABLE_ANISOTROPY) {
                    features.samplerAnisotropy(true)
                }

                ptrBuf.clear()
                val VK_KHR_SWAPCHAIN_EXTENSION = stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                ptrBuf.put(VK_KHR_SWAPCHAIN_EXTENSION)
                ptrBuf.flip()

                val layers = stack.pointers(1)
                layers.put(stack.UTF8("VK_LAYER_KHRONOS_validation"))

                val device_info = VkDeviceCreateInfo.malloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(queue)
                    .pEnabledFeatures(features)
                    .ppEnabledLayerNames(layers)
                    .ppEnabledExtensionNames(ptrBuf)

                val code = VK13.vkCreateDevice(physicalDevice.physicalDevice, device_info, null, pp)
                if (code != VK13.VK_SUCCESS) {
                    throw IllegalStateException("failed to create logical dvice!")
                }
                device = VkDevice(pp[0], physicalDevice.physicalDevice, device_info)

                VK13.vkGetDeviceQueue(device, physicalDevice.graphicsFamily, 0, pp)
                graphicsQueue = VkQueue(pp[0], device)
                graphicsFamily = physicalDevice.graphicsFamily
                presentFamily = physicalDevice.presentFamily

                memoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
                VK13.vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, memoryProperties)

            }

        }

    }

    override fun close() {
        VK13.vkDestroyDevice(device, null)
        memoryProperties.free()
    }

}