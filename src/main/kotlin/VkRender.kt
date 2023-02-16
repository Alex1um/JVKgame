import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import org.lwjgl.glfw.GLFWVulkan.*
import org.lwjgl.vulkan.VK13.vkEnumeratePhysicalDevices
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.charset.StandardCharsets
import kotlin.properties.Delegates

class VkRender {

    var window: Long? = null
    var height = 600
    var width = 800
    private val ip = MemoryUtil.memAllocInt(1)
    private val lp = MemoryUtil.memAllocLong(1)
    private val pp = MemoryUtil.memAllocPointer(1)
    private val extensionNames = MemoryUtil.memAllocPointer(64)
    private lateinit var device: VkDevice

    private lateinit var instance: VkInstance
    private var surface: Long = 0;
    private lateinit var physicalDevice: VkPhysicalDevice
    private val gpu_props = VkPhysicalDeviceProperties.malloc()
    private val gpu_features = VkPhysicalDeviceFeatures.malloc()
    private lateinit var queue_props: VkQueueFamilyProperties.Buffer
    private lateinit var graphicsQueue: VkQueue
    private val KHR_swapchain = MemoryUtil.memASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
//    private lateinit var surfaceFormat: VkSurfaceFormatKHR
//    private var presentMode by Delegates.notNull<Int>()
//    private lateinit var extent: VkExtent2D
    private var imageCount by Delegates.notNull<Int>()
    private var swapChain: Long = 0
//    private var swapChainImageCount by Delegates.notNull<Int>()
    private lateinit var swapChainImages: LongBuffer;
    private var swapChainImageFormat by Delegates.notNull<Int>()
    private lateinit var swapChainExtent: VkExtent2D
    private lateinit var swapChainImageViews: LongArray

    inner class QueueFamilyIndices(stack: MemoryStack, physicalDevice: VkPhysicalDevice) {
        var graphicsFamily: Int? = null
        var presentFamily: Int? = null

        init {
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, null)
            val tmp_queue_props = VkQueueFamilyProperties.malloc(ip[0], stack)
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, tmp_queue_props)

            val supportsPresent = stack.mallocInt(tmp_queue_props.capacity())
            for (i in 0 until  tmp_queue_props.capacity()) {
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, supportsPresent)
                if (supportsPresent[i] == VK_TRUE) {
                    presentFamily = i
                    break
                }
            }
            for (i in 0 until  tmp_queue_props.capacity()) {
                if (tmp_queue_props[i].queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
                    graphicsFamily = i
                    break
                }
            }

        }

        fun isComplete(): Boolean {
            return graphicsFamily != null && presentFamily != null
        }
    }

    inner class SwapChainSupportDetails(stack: MemoryStack, physicalDevice: VkPhysicalDevice) {
        var capabilities: VkSurfaceCapabilitiesKHR
        var formats: VkSurfaceFormatKHR.Buffer
        var presentModes: IntBuffer

        init {
            capabilities = VkSurfaceCapabilitiesKHR.malloc(stack)
            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, capabilities)

            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, ip, null)
            presentModes = stack.mallocInt(ip[0])
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, ip, presentModes)

            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, ip, null)
            formats = VkSurfaceFormatKHR.malloc(ip[0], stack)
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, ip, formats)

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

    fun isDeviceSuitable(device: VkPhysicalDevice): Boolean {
        MemoryStack.stackPush().use { stack ->

            val indices = QueueFamilyIndices(stack, device)

            var extensionsSupported = false
            vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, ip, null)
            val props = VkExtensionProperties.malloc(ip[0], stack)
            vkEnumerateDeviceExtensionProperties(device, null as ByteBuffer?, ip, props)
            for (i in 0 until props.capacity()) {
                if (KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME == props[i].extensionNameString()) {
                    extensionsSupported = true
                    break
                }
            }

            var swapChainAdequate = false
            if (extensionsSupported) {
                val details = SwapChainSupportDetails(stack, device)
                swapChainAdequate = details.formats.capacity() != 0 && details.presentModes.capacity() != 0
            }

            return indices.isComplete() && extensionsSupported && swapChainAdequate
        }
    }

    fun run() {
        initWindow()
        initVulkan()
        mainLoop()
        cleanup()
    }

    private fun initWindow() {
        glfwInit()

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        window = glfwCreateWindow(width, height, "Vulkan window", 0, 0)
    }

    private fun initVulkan() {
        createInstance()
        createSurface()
        pickPhysicalDevice()
        createLogicalDevice()
        createSwapChain()
        createImageViews()
    }

    private fun createImageViews() {
        swapChainImageViews = LongArray(swapChainImages.capacity())
        MemoryStack.stackPush().use { stack ->
            for (i in 0 until swapChainImages.capacity()) {
                val createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(swapChainImages[i])
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(swapChainImageFormat)
                    .components {
                        it.a(VK_COMPONENT_SWIZZLE_IDENTITY)
                        it.r(VK_COMPONENT_SWIZZLE_IDENTITY)
                        it.g(VK_COMPONENT_SWIZZLE_IDENTITY)
                        it.b(VK_COMPONENT_SWIZZLE_IDENTITY)
                    }
                    .subresourceRange {
                        it.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                        it.baseMipLevel(0)
                        it.levelCount(1)
                        it.baseArrayLayer(0)
                        it.levelCount(1)
                    }
                if (vkCreateImageView(device, createInfo, null, lp) != VK_SUCCESS) {
                    throw IllegalStateException("failed to create image views!")
                }
                swapChainImageViews[i] = lp[0]
            }
        }
    }

    private fun createSwapChain() {
        MemoryStack.stackPush().use { stack ->
            val details = SwapChainSupportDetails(stack, physicalDevice)

            val surfaceFormat = details.chooseSwapSurfaceFormat()
            val presentMode = details.chooseSwapPresentMode()
            val extent = details.chooseSwapExtent()

            imageCount = details.capabilities.minImageCount() + 1
            if (details.capabilities.maxImageCount() in 1 until imageCount) {
                imageCount = details.capabilities.maxImageCount()
            }

            val createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .surface(surface)
                .minImageCount(imageCount)
                .imageFormat(surfaceFormat.format())
                .imageColorSpace(surfaceFormat.colorSpace())
                .imageExtent(extent)
                .imageArrayLayers(1)
                // TODO: to separate image
                .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .queueFamilyIndexCount(0)
                .pQueueFamilyIndices(null)
                .preTransform(details.capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .clipped(true)
                .oldSwapchain(swapChain)

            if (KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("Failed to create swap chain")
            }
            swapChain = lp[0]

            KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, ip, null)
//            val swapChainImageCount = ip[0]
            swapChainImages = MemoryUtil.memAllocLong(ip[0])
            KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, ip, swapChainImages)

            swapChainImageFormat = surfaceFormat.format()
            swapChainExtent = extent
        }
    }

    private fun createSurface() {
        if (glfwCreateWindowSurface(instance, window!!, null, lp) != VK_SUCCESS) {
            throw IllegalStateException("failed to create window surface")
        }
        surface = lp[0]
    }

    private fun pickPhysicalDevice() {
        MemoryStack.stackPush().use { stack ->
            vkEnumeratePhysicalDevices(instance, ip, null)
            if (ip[0] == 0) {
                throw IllegalStateException("failed to find GPUs with Vulkan support")
            }
            val physical_devices = stack.mallocPointer(ip[0])
            vkEnumeratePhysicalDevices(instance, ip, physical_devices)

            var is_gpu_found = false
            for (i in 0 until physical_devices.capacity()) {
                val tmp_dev = VkPhysicalDevice(physical_devices[i], instance)
                if (isDeviceSuitable(tmp_dev)) {
                    physicalDevice = tmp_dev
                    is_gpu_found = true
                    break
                }
            }
            if (!is_gpu_found) {
                throw IllegalStateException("Gpu with vulkan support not found")
            }

            vkGetPhysicalDeviceProperties(physicalDevice, gpu_props)
            vkGetPhysicalDeviceFeatures(physicalDevice, gpu_features)

            println("Using gpu: ${StandardCharsets.UTF_8.decode(gpu_props.deviceName()).toString().substringBefore("\u0000")}")

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, null)
            if (ip[0] == 0) {
                throw IllegalStateException("No family queues")
            }
            queue_props = VkQueueFamilyProperties.malloc(ip[0])
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, queue_props)

        }
    }


    private fun createLogicalDevice() {

        MemoryStack.stackPush().use { stack ->

            val indices = QueueFamilyIndices(stack, physicalDevice)
            if (indices.graphicsFamily != indices.presentFamily) {
                throw IllegalStateException("families are not same")
            }

            val queue = VkDeviceQueueCreateInfo.calloc(/*Queue count: */1 , stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .queueFamilyIndex(indices.graphicsFamily!!)
                .pQueuePriorities(stack.floats(1.0f))

            val features = VkPhysicalDeviceFeatures.calloc(stack)

            extensionNames.clear()
            extensionNames.put(KHR_swapchain)
            extensionNames.flip()

            val device_info = VkDeviceCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pQueueCreateInfos(queue)
                .pEnabledFeatures(features)
                .ppEnabledExtensionNames(extensionNames)
                .ppEnabledLayerNames(null)

            val code = vkCreateDevice(physicalDevice, device_info, null, pp)
            if (code != VK_SUCCESS) {
                throw IllegalStateException("failed to create logical dvice!")
            }
            device = VkDevice(pp[0], physicalDevice, device_info)

            vkGetDeviceQueue(device, indices.graphicsFamily!!, 0, pp)
            graphicsQueue = VkQueue(pp[0], device)
        }
    }

    private fun createInstance() {
        MemoryStack.stackPush().use { stack ->

            val APP_SHORT_NAME: ByteBuffer = stack.UTF8("Hello trianble")
            val appInfo: VkApplicationInfo = VkApplicationInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(APP_SHORT_NAME)
                .applicationVersion(VK_MAKE_VERSION(0, 0, 1))
                .pEngineName(APP_SHORT_NAME)
                .engineVersion(VK_MAKE_VERSION(0, 0, 1))
                .apiVersion(VK_API_VERSION_1_3)


            var createInfo = VkInstanceCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo)

            val glfwExtensions = glfwGetRequiredInstanceExtensions()
                ?: throw IllegalStateException("glfwGetRequiredInstanceExtensions failed to find the platform surface extensions.")

            createInfo = createInfo.ppEnabledExtensionNames(glfwExtensions)

            val result = vkCreateInstance(createInfo, null, pp)
            if (result != VK_SUCCESS) {
                throw IllegalStateException("failed to create instance!")
            }
            instance = VkInstance(pp[0], createInfo)
        }
    }

    private fun mainLoop() {
        while (!glfwWindowShouldClose(window!!)) {
            glfwPollEvents()
        }
    }

    private fun cleanup() {
        for (e in swapChainImageViews) {
            vkDestroyImageView(device, e, null)
        }
        MemoryUtil.memFree(swapChainImages)
        KHRSwapchain.vkDestroySwapchainKHR(device, swapChain, null)
        vkDestroyDevice(device, null)
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null)
        vkDestroyInstance(instance, null)
        glfwDestroyWindow(window!!)
        glfwTerminate()
        MemoryUtil.memFree(extensionNames)
        MemoryUtil.memFree(pp)
        MemoryUtil.memFree(lp)
        MemoryUtil.memFree(ip)
        MemoryUtil.memFree(KHR_swapchain)
    }


}