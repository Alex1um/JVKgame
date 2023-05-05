package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import java.io.Closeable

class RenderPass(private val ldevice: Device, pdevice: PhysicalDevice) : Closeable {

    val renderPass: Long

    init {
        MemoryStack.stackPush().use { stack ->
            var format = pdevice.surfaceFormat.format()
            if (format == VK13.VK_FORMAT_UNDEFINED) {
                format = VK13.VK_FORMAT_B8G8R8A8_SRGB
            }
            val colorAttachment = VkAttachmentDescription.calloc(2, stack)
            colorAttachment[0]
                .format(format)
                .samples(VK13.VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK13.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK13.VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK13.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK13.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK13.VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(VK13.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            colorAttachment[1]
                .format(format)
                .samples(VK13.VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK13.VK_ATTACHMENT_LOAD_OP_LOAD)
                .storeOp(VK13.VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK13.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK13.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK13.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)

            val colorAttachmentRef = VkAttachmentReference.calloc(1, stack)
            colorAttachmentRef[0]
                .attachment(0)
                .layout(VK13.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            val colorAttachmentMiniMapRef = VkAttachmentReference.calloc(1, stack)
            colorAttachmentMiniMapRef[0]
                .attachment(0)
                .layout(VK13.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

            val subpassDescriptions = VkSubpassDescription.calloc(2, stack)
            subpassDescriptions[0]
                .pipelineBindPoint(VK13.VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(colorAttachmentRef)
            subpassDescriptions[1]
                .pipelineBindPoint(VK13.VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(colorAttachmentMiniMapRef)

            val dependency = VkSubpassDependency.calloc(1, stack)
                .srcSubpass(0)
                .dstSubpass(1)
                .srcStageMask(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                .dstStageMask(VK13.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
                .srcAccessMask(VK13.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
                .dstAccessMask(VK13.VK_ACCESS_SHADER_READ_BIT)
                .dependencyFlags(VK13.VK_DEPENDENCY_BY_REGION_BIT)

//                .srcSubpass(VK13.VK_SUBPASS_EXTERNAL)
//                .dstSubpass(0)
//                .srcStageMask(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//                .dstStageMask(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//                .srcAccessMask(0)
//                .dstAccessMask(VK13.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
//                .dependencyFlags(VK13.VK_DEPENDENCY_BY_REGION_BIT)

            val renderPassInfo = VkRenderPassCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pAttachments(colorAttachment)
                .pSubpasses(subpassDescriptions)
                .pDependencies(dependency)


            if (VK13.vkCreateRenderPass(ldevice.device, renderPassInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create render pass")
            }
            renderPass = Util.lp[0]
        }
    }

    override fun close() {
        VK13.vkDestroyRenderPass(ldevice.device, renderPass, null)
    }

}