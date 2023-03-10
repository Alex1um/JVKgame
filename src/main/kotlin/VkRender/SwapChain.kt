package VkRender

import VkRender.Surfaces.Surface
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.io.Closeable
import java.nio.IntBuffer
import java.nio.LongBuffer

class SwapChain(
    val ldevice: Device,
    val pdevice: PhysicalDevice,
    val sfc: Surface,
    val renderPass: RenderPass,
    width: Int,
    height: Int,
    ) : Closeable {

    var swapChain: Long = 0
        private set;
    private var imageCount: Int = 0
    private lateinit var swapChainImages: LongBuffer
    private var swapChainImageFormat: Int = 0
    lateinit var swapChainImageViews: LongArray
    private lateinit var swapChainExtent: VkExtent2D
    lateinit var swapChainFramebuffers: LongArray
        private set;

    init {
        Create(width, height)
    }

    private fun Create(width: Int,
                       height: Int,
                       ) {
        with(Util) {
            MemoryStack.stackPush().use { stack ->

                val details = SwapChainSupportDetails(stack, pdevice.physicalDevice, sfc)

                val surfaceFormat = details.chooseSwapSurfaceFormat()
                val presentMode = details.chooseSwapPresentMode()
                val extent = details.chooseSwapExtent()
                if (extent.width() == -1 || extent.height() == -1) {
                    extent.width(width)
                    extent.height(height)
                }

                val minimg = details.capabilities.minImageCount() + 1
                imageCount = if (details.capabilities.maxImageCount() in 1 until minimg) {
                    details.capabilities.maxImageCount()
                } else {
                    minimg
                }

                var createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(sfc.surface)
                    .minImageCount(imageCount)
                    .imageFormat(surfaceFormat.format())
                    .imageColorSpace(surfaceFormat.colorSpace())
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    // TODO: to separate image
                    .imageUsage(VK13.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .preTransform(details.capabilities.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true)
                    .oldSwapchain(swapChain)

                if (pdevice.graphicsFamily != pdevice.presentFamily) {
                    val b = IntBuffer.allocate(2)
                    b.put(pdevice.graphicsFamily)
                    b.put(pdevice.presentFamily)
                    createInfo = createInfo.imageSharingMode(VK13.VK_SHARING_MODE_CONCURRENT)
                        .queueFamilyIndexCount(2)
                        .pQueueFamilyIndices(b)
                } else {
                    createInfo = createInfo.imageSharingMode(VK13.VK_SHARING_MODE_EXCLUSIVE)
                        .queueFamilyIndexCount(0)
                        .pQueueFamilyIndices(null)
                }

                with(ldevice) {
                    if (KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, lp) != VK13.VK_SUCCESS) {
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
                createImageViews(stack)
                createFrameBuffers(stack, renderPass.renderPass, width, height)
            }
        }
    }

    private fun createImageViews(stack: MemoryStack) {
        swapChainImageViews = LongArray(swapChainImages.capacity())
        for (i in 0 until swapChainImages.capacity()) {
            val createInfo = VkImageViewCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(swapChainImages[i])
                .viewType(VK13.VK_IMAGE_VIEW_TYPE_2D)
                .format(swapChainImageFormat)
                .components {
                    it.a(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
                    it.r(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
                    it.g(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
                    it.b(VK13.VK_COMPONENT_SWIZZLE_IDENTITY)
                }
                .subresourceRange {
                    it.aspectMask(VK13.VK_IMAGE_ASPECT_COLOR_BIT)
                    it.baseMipLevel(0)
                    it.levelCount(1)
                    it.baseArrayLayer(0)
                    it.levelCount(1)
                    it.layerCount(1)
                }
            if (VK13.vkCreateImageView(ldevice.device, createInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create image views!")
            }
            swapChainImageViews[i] = Util.lp[0]
        }
    }

    private fun createFrameBuffers(stack: MemoryStack, renderPass: Long, width: Int, height: Int) {
        swapChainFramebuffers = LongArray(swapChainImageViews.size)
        for (i in swapChainImageViews.indices) {
            val attachments = stack.longs(swapChainImageViews[i])

            val framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .renderPass(renderPass)
                .attachmentCount(1)
                .pAttachments(attachments)
                .width(width)
                .height(height)
                .layers(1)

            if (VK13.vkCreateFramebuffer(ldevice.device, framebufferInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create framebuffer")
            }
            swapChainFramebuffers[i] = Util.lp[0]
        }
    }

    fun reCreate(newWidth: Int, newHeight: Int) {

        VK13.vkDeviceWaitIdle(ldevice.device)

        val old = swapChain
        Create(newWidth, newHeight)
        KHRSwapchain.vkDestroySwapchainKHR(ldevice.device, old, null)

    }

    override fun close() {
        for (framebuffer in swapChainFramebuffers) {
            VK13.vkDestroyFramebuffer(ldevice.device, framebuffer, null)
        }
        for (e in swapChainImageViews) {
            VK13.vkDestroyImageView(ldevice.device, e, null)
        }
        MemoryUtil.memFree(swapChainImages)
        KHRSwapchain.vkDestroySwapchainKHR(ldevice.device, swapChain, null)
    }
}