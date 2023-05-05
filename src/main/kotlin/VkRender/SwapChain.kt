package VkRender

import VkRender.Surfaces.Surface
import VkRender.Textures.ImageView
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
    lateinit var swapChainImageViews: Array<ImageView>
    private lateinit var swapChainExtent: VkExtent2D
    lateinit var swapChainFramebuffers: LongArray
        private set;
    lateinit var swapChainMiniMapFramebuffers: LongArray
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
//                createImageViews(stack)
                swapChainImageViews = Array(swapChainImages.capacity()) { ImageView(ldevice, stack, swapChainImages[it], swapChainImageFormat) }
                createFrameBuffers(stack, renderPass.renderPass, width, height)
            }
        }
    }

    private fun createFrameBuffers(stack: MemoryStack, renderPass: Long, width: Int, height: Int) {
        swapChainFramebuffers = LongArray(swapChainImageViews.size)
        for (i in swapChainImageViews.indices) {
            val attachments = stack.longs(swapChainImageViews[i].view, swapChainImageViews[i].view)

            val framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .renderPass(renderPass)
                .attachmentCount(2)
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
            e.close()
        }
        MemoryUtil.memFree(swapChainImages)
        KHRSwapchain.vkDestroySwapchainKHR(ldevice.device, swapChain, null)
    }
}