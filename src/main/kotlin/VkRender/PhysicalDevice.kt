package VkRender


import VkRender.Surfaces.Surface
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*


import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.properties.Delegates


class PhysicalDevice(vkinstance: Instance, surface: Surface) : Closeable {

    val properties: VkPhysicalDeviceProperties = VkPhysicalDeviceProperties.malloc()
    val features: VkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.malloc()
    val queueProperties: VkQueueFamilyProperties.Buffer

    val extent: VkExtent2D
    val presentMode: Int
    val surfaceFormat: VkSurfaceFormatKHR

    var graphicsFamily by Delegates.notNull<Int>()
    var presentFamily by Delegates.notNull<Int>()

    lateinit var physicalDevice: VkPhysicalDevice
        private set

    private fun isDeviceSuitable(device: VkPhysicalDevice, indices: QueueFamilyIndices, surface: Surface): Boolean {

        with(Util) {

            MemoryStack.stackPush().use { stack ->


//            val ip = stack.ints(0)

                var extensionsSupported = false
                VK13.vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, ip, null)
                val props = VkExtensionProperties.malloc(ip[0], stack)
                VK13.vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, ip, props)
                for (i in 0 until props.capacity()) {
                    if (KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME == props[i].extensionNameString()) {
                        extensionsSupported = true
                        break
                    }

                }

                var swapChainAdequate = false
                if (extensionsSupported) {
                    val details = SwapChainSupportDetails(stack, device, surface)
                    swapChainAdequate = details.formats.capacity() != 0 && details.presentModes.capacity() != 0
                }

                return indices.isComplete() && extensionsSupported && swapChainAdequate
            }
        }
    }

    init {
        with(Util) {
            with(vkinstance) {
                MemoryStack.stackPush().use { stack ->

                    VK13.vkEnumeratePhysicalDevices(instance, ip, null)
                    if (ip[0] == 0) {
                        throw IllegalStateException("failed to find GPUs with Vulkan support")
                    }

                    val physicalDevices = stack.mallocPointer(ip[0])
                    VK13.vkEnumeratePhysicalDevices(instance, ip, physicalDevices)

                    var is_gpu_found = false
                    for (i in 0 until physicalDevices.capacity()) {
                        val tmp_dev = VkPhysicalDevice(physicalDevices[i], instance)

                        val indices = QueueFamilyIndices(stack, tmp_dev, surface)

                        if (isDeviceSuitable(tmp_dev, indices, surface)) {
                            physicalDevice = tmp_dev
                            is_gpu_found = true
                            graphicsFamily = indices.graphicsFamily!!
                            presentFamily = indices.presentFamily!!
                            break
                        }
                    }
                    if (!is_gpu_found) {
                        throw IllegalStateException("Gpu with vulkan support not found")
                    }

                    VK13.vkGetPhysicalDeviceProperties(physicalDevice, properties)
                    VK13.vkGetPhysicalDeviceFeatures(physicalDevice, features)

                    println(
                        "Using gpu: ${
                            StandardCharsets.UTF_8.decode(properties.deviceName()).toString().substringBefore("\u0000")
                        }"
                    )

                    VK13.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, null)
                    if (ip[0] == 0) {
                        throw IllegalStateException("No family queues")
                    }
                    queueProperties = VkQueueFamilyProperties.malloc(ip[0])
                    VK13.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, queueProperties)

                    val details = SwapChainSupportDetails(stack, physicalDevice, surface)

                    extent = details.chooseSwapExtent()
                    presentMode = details.chooseSwapPresentMode()
                    surfaceFormat = details.chooseSwapSurfaceFormat()

                }
            }
        }
    }

    override fun close() {
        properties.free()
        features.free()
        queueProperties.free()
    }

}