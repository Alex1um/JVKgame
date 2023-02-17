import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import org.lwjgl.glfw.GLFWVulkan.*
import org.lwjgl.vulkan.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.charset.StandardCharsets
import kotlin.properties.Delegates

class VkRender {

    var window: Long? = null
    var height = 600
    var width = 800
    private val MAX_FRAMES_IN_FLIGHT = 2
    private var framebufferResized = false

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
    private var imageCount by Delegates.notNull<Int>()
    private var swapChain: Long = 0
    private lateinit var swapChainImages: LongBuffer;
    private var swapChainImageFormat by Delegates.notNull<Int>()
    private lateinit var swapChainExtent: VkExtent2D
    private lateinit var swapChainImageViews: LongArray
    private var renderPass by Delegates.notNull<Long>()
    private var pipelineLayout by Delegates.notNull<Long>()
    private var graphicsPipeLine by Delegates.notNull<Long>()
    private lateinit var swapChainFramebuffers: LongArray
    private var commandPool by Delegates.notNull<Long>()
    private val commandBuffer: Array<VkCommandBuffer?> = arrayOfNulls(MAX_FRAMES_IN_FLIGHT)
    private val imageAvailableSemaphore = LongArray(MAX_FRAMES_IN_FLIGHT)
    private var renderFinishedSemaphore = LongArray(MAX_FRAMES_IN_FLIGHT)
    private var inFlightFence = LongArray(MAX_FRAMES_IN_FLIGHT)
    private var currentFrame = 0

    inner class QueueFamilyIndices(stack: MemoryStack, physicalDevice: VkPhysicalDevice) {
        var graphicsFamily: Int? = null
        var presentFamily: Int? = null

        init {
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, null)
            val tmp_queue_props = VkQueueFamilyProperties.malloc(ip[0], stack)
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, ip, tmp_queue_props)

            val supportsPresent = stack.mallocInt(tmp_queue_props.capacity())
            for (i in 0 until  tmp_queue_props.capacity()) {
                supportsPresent.position(i)
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
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(width, height, "Vulkan window", 0, 0)
        glfwSetFramebufferSizeCallback(window!!) { window, width, height ->
            framebufferResized = true
        }
    }

    private fun initVulkan() {
        createInstance()
        createSurface()
        pickPhysicalDevice()
        createLogicalDevice()
        createSwapChain()
        createImageViews()
        createRenderPass()
        createGraphicsPipeline()
        createFrameBuffers()
        createCommandPool()
        createCommandBuffer()
        createSyncObjects()
    }

    private fun createSyncObjects() {
        MemoryStack.stackPush().use { stack ->

            val semaphore = VkSemaphoreCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)

            val fenceInfo = VkFenceCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                .flags(VK_FENCE_CREATE_SIGNALED_BIT)
                .pNext(MemoryUtil.NULL)

            if (vkCreateSemaphore(device, semaphore, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create semaphore!")
            }
            imageAvailableSemaphore[0] = lp[0]

            if (vkCreateSemaphore(device, semaphore, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create semaphore!")
            }
            imageAvailableSemaphore[1] = lp[0]

            if (vkCreateSemaphore(device, semaphore, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create semaphore!")
            }
            renderFinishedSemaphore[0] = lp[0]

            if (vkCreateSemaphore(device, semaphore, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create semaphore!")
            }
            renderFinishedSemaphore[1] = lp[0]

            if (vkCreateFence(device, fenceInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create fence!")
            }
            inFlightFence[0] = lp[0]

            if (vkCreateFence(device, fenceInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create fence!")
            }
            inFlightFence[1] = lp[0]
        }
    }

    private fun recordCommandBuffer(commandBuffer: VkCommandBuffer, imageIndex: Int) {
        MemoryStack.stackPush().use { stack ->
            val beginInfo = VkCommandBufferBeginInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .pInheritanceInfo(null)

            if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                throw IllegalStateException("failed to begin recording command buffer!");
            }

            val clearColor = VkClearValue.calloc(1, stack)
            clearColor[0].color()
                .float32(0, 0.0f)
                .float32(1, 0.0f)
                .float32(2, 0.0f)
                .float32(3, 1.0f)

            val renderPassInfo = VkRenderPassBeginInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .renderPass(renderPass)
                .framebuffer(swapChainFramebuffers[imageIndex])
                .renderArea {
                    it.offset {
                        it.x(0)
                            .y(0)
                    }
                    it.extent {
                        it.width(width)
                            .height(height)
                    }
                }
                .clearValueCount(1)
                .pClearValues(clearColor)
                .pNext(MemoryUtil.NULL)

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE)
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeLine)

            val viewport = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(width.toFloat())
                .height(height.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            vkCmdSetViewport(commandBuffer, 0, viewport)

            val scissor = VkRect2D.calloc(1, stack)
                .offset {
                    it.x(0)
                        .y(0)
                }
//                .extent(swapChainExtent)
                .extent {
                    it.width(width)
                        .height(height)
                }

            vkCmdSetScissor(commandBuffer, 0, scissor)

            vkCmdDraw(commandBuffer, 3, 1, 0, 0)
            vkCmdEndRenderPass(commandBuffer)
            if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                throw IllegalStateException("failed to record command buffer")
            }
        }
    }
    private fun createCommandBuffer() {
        MemoryStack.stackPush().use { stack ->
            val allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(MAX_FRAMES_IN_FLIGHT)

            val pp2 = stack.pointers(0, 0)

            if (vkAllocateCommandBuffers(device, allocInfo, pp2) != VK_SUCCESS) {
                throw IllegalStateException("failed to allocate command buffers!");
            }
            for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
                commandBuffer[i] = VkCommandBuffer(pp2[i], device)
            }
        }
    }

    private fun createCommandPool() {
        MemoryStack.stackPush().use { stack ->
            val indices = QueueFamilyIndices(stack, physicalDevice)
            val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                .queueFamilyIndex(indices.graphicsFamily!!)

            if (vkCreateCommandPool(device, poolInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create command pool!")
            }
            commandPool = lp[0]
        }
    }

    private fun createFrameBuffers() {
        swapChainFramebuffers = LongArray(swapChainImageViews.size)
        MemoryStack.stackPush().use { stack ->
            for (i in swapChainImageViews.indices) {
                val attachments = stack.longs(swapChainImageViews[i])

                val framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(renderPass)
                    .attachmentCount(1)
                    .pAttachments(attachments)
                    .width(width)
                    .height(height)
                    .layers(1)

                if (vkCreateFramebuffer(device, framebufferInfo, null, lp) != VK_SUCCESS) {
                    throw IllegalStateException("failed to create framebuffer")
                }
                swapChainFramebuffers[i] = lp[0]
            }
        }
    }

    private fun createRenderPass() {
        MemoryStack.stackPush().use { stack ->
            val colorAttachment = VkAttachmentDescription.calloc(1, stack)
                .format(swapChainImageFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)

            val colorAttachmentRef = VkAttachmentReference.calloc(1, stack)
                .attachment(0)
                .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

            val subpass = VkSubpassDescription.calloc(1, stack)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(colorAttachmentRef)


            val dependency = VkSubpassDependency.calloc(1, stack)
                .srcSubpass(VK_SUBPASS_EXTERNAL)
                .dstSubpass(0)
                .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                .srcAccessMask(0)
                .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)

            val renderPassInfo = VkRenderPassCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pAttachments(colorAttachment)
                .pSubpasses(subpass)
                .pDependencies(dependency)


            if (vkCreateRenderPass(device, renderPassInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create render pass")
            }
            renderPass = lp[0]
        }
    }

    private fun readShader(filename: String): ByteArray {
        with(File(filename)) {
            return this.readBytes()
        }
    }

    private fun createShaderModule(code: ByteArray): Long {
        MemoryStack.stackPush().use { stack ->
            val pCode = MemoryUtil.memAlloc(code.size).put(code)
            pCode.flip()
            val createInfo = VkShaderModuleCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(pCode)
                .flags(0)
                .pNext(MemoryUtil.NULL)
            val rt = vkCreateShaderModule(device, createInfo, null, lp)
            if (rt != VK_SUCCESS) {
                throw IllegalStateException("failed to create shader module!")
            }
            MemoryUtil.memFree(pCode)
            return lp[0]
        }
    }
    private fun createGraphicsPipeline() {
        val vertShaderCode = readShader("build/resources/main/shaders/vert.spv")
        val fragShaderCode = readShader("build/resources/main/shaders/frag.spv")

        val vertShaderModule = createShaderModule(vertShaderCode)
        val fragShaderModule = createShaderModule(fragShaderCode)

        MemoryStack.stackPush().use { stack ->
            val shaderStagesInfo = VkPipelineShaderStageCreateInfo.calloc(2, stack)

            val main = stack.UTF8("main")

            shaderStagesInfo[0]
                .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(VK_SHADER_STAGE_VERTEX_BIT)
                .module(vertShaderModule)
                .pName(main)
            shaderStagesInfo[1]
                .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                .module(fragShaderModule)
                .pName(main)

            val dynamicStates = stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR)

            val dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                .pDynamicStates(dynamicStates)


            val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexBindingDescriptions(null)
                .pVertexAttributeDescriptions(null)

            val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                .primitiveRestartEnable(false)

            val viewPort = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(width.toFloat())
                .height(height.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            val scissor = VkRect2D.calloc(1, stack)
                .offset{
                    it.x(0)
                        .y(0)
                }
//                .extent(swapChainExtent)
                .extent {
                    it.width(width)
                        .height(height)
                }

            val viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .viewportCount(1)
                .scissorCount(1)
                .pScissors(scissor)
                .pViewports(viewPort)

            val rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false)
                .polygonMode(VK_POLYGON_MODE_FILL)
                .lineWidth(1.0f)
                .cullMode(VK_CULL_MODE_BACK_BIT)
                .frontFace(VK_FRONT_FACE_CLOCKWISE)
                .depthBiasEnable(false)
                .depthBiasConstantFactor(0.0f)
                .depthBiasClamp(0.0f)
                .depthBiasSlopeFactor(0.0f)

            val multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .sampleShadingEnable(false)
                .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
                .minSampleShading(1.0f)
                .pSampleMask(null)
                .alphaToCoverageEnable(false)
                .alphaToOneEnable(false)

            val colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
                .colorWriteMask(VK_COLOR_COMPONENT_A_BIT or VK_COLOR_COMPONENT_B_BIT or VK_COLOR_COMPONENT_G_BIT or VK_COLOR_COMPONENT_R_BIT)
//                .blendEnable(false)
//                .srcColorBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstColorBlendFactor(VK_BLEND_FACTOR_ZERO)
//                .colorBlendOp(VK_BLEND_OP_ADD)
//                .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
//                .alphaBlendOp(VK_BLEND_OP_ADD)

                .blendEnable(true)
                .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
                .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                .colorBlendOp(VK_BLEND_OP_ADD)
                .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
                .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
                .alphaBlendOp(VK_BLEND_OP_ADD)

            val colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .logicOpEnable(false)
                .logicOp(VK_LOGIC_OP_COPY)
                .pAttachments(colorBlendAttachment)
                .blendConstants(0, 0.0f)
                .blendConstants(1, 0.0f)
                .blendConstants(2, 0.0f)
                .blendConstants(3, 0.0f)

            val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                .pSetLayouts(null)
                .pPushConstantRanges(null)

            if (vkCreatePipelineLayout(device, pipelineLayoutInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create pipeline layout!")
            }
            pipelineLayout = lp[0]

            val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pStages(shaderStagesInfo)
                .pVertexInputState(vertexInputInfo)
                .pInputAssemblyState(inputAssembly)
                .pViewportState(viewportState)
                .pRasterizationState(rasterizer)
                .pMultisampleState(multisampling)
                .pDepthStencilState(null)
                .pColorBlendState(colorBlending)
                .pDynamicState(dynamicState)
                .layout(pipelineLayout)
                .renderPass(renderPass)
                .subpass(0)
                .basePipelineHandle(VK_NULL_HANDLE)
                .basePipelineIndex(-1)

            if (vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, lp) != VK_SUCCESS) {
                throw IllegalStateException("failed to create graphics pipeline")
            }
            graphicsPipeLine = lp[0]
        }

        vkDestroyShaderModule(device, vertShaderModule, null)
        vkDestroyShaderModule(device, fragShaderModule, null)
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
                        it.layerCount(1)
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

            var createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .surface(surface)
                .minImageCount(imageCount)
                .imageFormat(surfaceFormat.format())
                .imageColorSpace(surfaceFormat.colorSpace())
                .imageExtent(extent)
                .imageArrayLayers(1)
                // TODO: to separate image
                .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(details.capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .clipped(true)
                .oldSwapchain(swapChain)

            val indices = QueueFamilyIndices(stack, physicalDevice)
            if (indices.graphicsFamily != indices.presentFamily) {
                val b = IntBuffer.allocate(2)
                b.put(indices.graphicsFamily!!)
                b.put(indices.presentFamily!!)
                createInfo = createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                    .queueFamilyIndexCount(2)
                    .pQueueFamilyIndices(b)
            } else {

                createInfo = createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .queueFamilyIndexCount(0)
                    .pQueueFamilyIndices(null)
            }

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
//            if (indices.graphicsFamily != indices.presentFamily) {
//                throw IllegalStateException("families are not same")
//            }

            val queue = if (indices.graphicsFamily!! == indices.presentFamily!!) {
                VkDeviceQueueCreateInfo.calloc(/*Queue count: */1, stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(indices.graphicsFamily!!)
                    .pQueuePriorities(stack.floats(1.0f))
            } else {
                val q = VkDeviceQueueCreateInfo.calloc(/*Queue count: */2, stack)
                q
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(indices.graphicsFamily!!)
                    .pQueuePriorities(stack.floats(1.0f))
                q
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(indices.presentFamily!!)
                    .pQueuePriorities(stack.floats(1.0f))
                q
            }

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

            extensionNames.clear()
            extensionNames.put(stack.ASCII("VK_LAYER_KHRONOS_validation"))
            extensionNames.flip()

            var createInfo = VkInstanceCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo)
                .ppEnabledLayerNames(extensionNames)

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
            drawFrame()
        }

        vkDeviceWaitIdle(device)
    }

    fun recreateSwapChain() {

        vkDeviceWaitIdle(device)

        MemoryStack.stackPush().use { stack ->
            val ip2 = stack.ints(1)
            glfwGetFramebufferSize(window!!, ip, ip2)
            while (ip[0] == 0 || ip2[0] == 0) {
                glfwGetFramebufferSize(window!!, ip, ip2)
                glfwWaitEvents()
            }
            width = ip[0]
            height = ip2[0]
        }

        vkDeviceWaitIdle(device)

        cleanupSwapChain()

        createSwapChain()
        createImageViews()
        createFrameBuffers()
    }

    fun drawFrame() {
        var imageIndex = 0;
        MemoryStack.stackPush().use { stack ->
            vkWaitForFences(device, inFlightFence[currentFrame], true, Long.MAX_VALUE)
//            vkResetFences(device, inFlightFence)
            var result = KHRSwapchain.vkAcquireNextImageKHR(
                device,
                swapChain,
                Long.MAX_VALUE,
                imageAvailableSemaphore[currentFrame],
                VK_NULL_HANDLE,
                ip
            )

            if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                recreateSwapChain()
                return;
            } else if (result != VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                throw IllegalStateException("failed to aquire swap chain image!")
            }

            vkResetFences(device, inFlightFence[currentFrame])

            imageIndex = ip[0]
            vkResetCommandBuffer(commandBuffer[currentFrame]!!, 0)

            recordCommandBuffer(commandBuffer[currentFrame]!!, imageIndex)

            val lp2 = stack.mallocLong(1)
            val submitInfo = VkSubmitInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(MemoryUtil.NULL)
                .waitSemaphoreCount(1)
                .pWaitSemaphores(lp.put(0, imageAvailableSemaphore[currentFrame]))
                .pWaitDstStageMask(ip.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(pp.put(0, commandBuffer[currentFrame]!!))
                .pSignalSemaphores(lp2.put(0, renderFinishedSemaphore[currentFrame]))

            if (vkQueueSubmit(graphicsQueue, submitInfo, inFlightFence[currentFrame]) != VK_SUCCESS) {
                throw IllegalStateException("failed to submit draw command buffer")
            }

            val presentInfo = VkPresentInfoKHR.malloc(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pWaitSemaphores(lp2)
                .swapchainCount(1)
                .pSwapchains(lp.put(0, swapChain))
                .pImageIndices(ip.put(0, imageIndex))
                .pResults(null)

            result = KHRSwapchain.vkQueuePresentKHR(graphicsQueue, presentInfo)

            if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                framebufferResized = false
                recreateSwapChain()
            } else if (result != VK_SUCCESS) {
                throw IllegalStateException("failed to present swap chain image!")
            }
            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT
        }
    }

    private fun cleanupSwapChain() {
        for (framebuffer in swapChainFramebuffers) {
            vkDestroyFramebuffer(device, framebuffer, null)
        }
        for (e in swapChainImageViews) {
            vkDestroyImageView(device, e, null)
        }
        KHRSwapchain.vkDestroySwapchainKHR(device, swapChain, null)
    }
    private fun cleanup() {
        for (i in 0 until MAX_FRAMES_IN_FLIGHT) {
            vkDestroySemaphore(device, imageAvailableSemaphore[i], null)
            vkDestroySemaphore(device, renderFinishedSemaphore[i], null)
            vkDestroyFence(device, inFlightFence[i], null)
        }
        vkDestroyCommandPool(device, commandPool, null)
        for (framebuffer in swapChainFramebuffers) {
            vkDestroyFramebuffer(device, framebuffer, null)
        }
        vkDestroyPipeline(device, graphicsPipeLine, null)
        vkDestroyPipelineLayout(device, pipelineLayout, null)
        vkDestroyRenderPass(device, renderPass, null)
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