package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.io.Closeable

class CommandPool(private val ldevice: Device, pdevice: PhysicalDevice) : Closeable {

    val pool: Long
    val commandBuffer: Array<VkCommandBuffer?> = arrayOfNulls(Config.MAX_FRAMES_IN_FLIGHT)

    init {

        MemoryStack.stackPush().use { stack ->
            val poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .flags(VK13.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                .queueFamilyIndex(pdevice.graphicsFamily)

            if (VK13.vkCreateCommandPool(ldevice.device, poolInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create command pool!")
            }
            pool = Util.lp[0]

            val allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(pool)
                .level(VK13.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(Config.MAX_FRAMES_IN_FLIGHT)

            val pp2 = stack.callocPointer(Config.MAX_FRAMES_IN_FLIGHT)

            if (VK13.vkAllocateCommandBuffers(ldevice.device, allocInfo, pp2) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to allocate command buffers!");
            }
            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                commandBuffer[i] = VkCommandBuffer(pp2[i], ldevice.device)
            }
        }
    }

    fun record(currentFrame: Int, imageIndex: Int, pass: RenderPass, pipeline: GraphicsPipeline, swapChain: SwapChain, width: Int, height: Int) {

        MemoryStack.stackPush().use { stack ->
            val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
//                .flags(0)
//                .pInheritanceInfo(null)

            if (VK13.vkBeginCommandBuffer(commandBuffer[currentFrame]!!, beginInfo) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to begin recording command buffer!");
            }

            val clearColor = VkClearValue.calloc(1, stack)
            clearColor.color()
                .float32(0, 0.0f)
                .float32(1, 0.0f)
                .float32(2, 0.0f)
                .float32(3, 1.0f)

            val renderPassInfo = VkRenderPassBeginInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .renderPass(pass.renderPass)
                .framebuffer(swapChain.swapChainFramebuffers[imageIndex])
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
//                .pNext(MemoryUtil.NULL)

            VK13.vkCmdBeginRenderPass(commandBuffer[currentFrame]!!, renderPassInfo, VK13.VK_SUBPASS_CONTENTS_INLINE)
            VK13.vkCmdBindPipeline(commandBuffer[currentFrame]!!, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.graphicsPipeLine)

            val viewport = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(width.toFloat())
                .height(height.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            VK13.vkCmdSetViewport(commandBuffer[currentFrame]!!, 0, viewport)

            val scissor = VkRect2D.calloc(1, stack)
                .offset {
                    it.x(0)
                        .y(0)
                }
                .extent {
                    it.width(width)
                        .height(height)
                }

            VK13.vkCmdSetScissor(commandBuffer[currentFrame]!!, 0, scissor)

            VK13.vkCmdDraw(commandBuffer[currentFrame]!!, 3, 1, 0, 0)
            VK13.vkCmdEndRenderPass(commandBuffer[currentFrame]!!)
            if (VK13.vkEndCommandBuffer(commandBuffer[currentFrame]!!) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to record command buffer")
            }
        }
    }

    override fun close() {
        VK13.vkDestroyCommandPool(ldevice.device, pool, null)
    }

}