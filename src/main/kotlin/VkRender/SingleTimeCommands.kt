package VkRender

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import java.io.Closeable

class SingleTimeCommands(val stack: MemoryStack, val ldevide: Device, val commands: CommandPool) : Closeable {

    val pCommandBuffer: PointerBuffer = stack.pointers(0)
    val commandBuffer: VkCommandBuffer

    init {
        val allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            .level(VK13.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
            .commandPool(commands.pool)
            .commandBufferCount(1)

        VK13.vkAllocateCommandBuffers(ldevide.device, allocInfo, pCommandBuffer)
        commandBuffer = VkCommandBuffer(pCommandBuffer[0], ldevide.device)

        val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            .flags(VK13.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

        VK13.vkBeginCommandBuffer(commandBuffer, beginInfo)
    }

    override fun close() {
        VK13.vkEndCommandBuffer(commandBuffer)

        val submitInfo = VkSubmitInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_SUBMIT_INFO)
            .pCommandBuffers(pCommandBuffer)

        VK13.vkQueueSubmit(ldevide.graphicsQueue, submitInfo, VK13.VK_NULL_HANDLE)
        VK13.vkQueueWaitIdle(ldevide.graphicsQueue)
        VK13.vkFreeCommandBuffers(ldevide.device, commands.pool, pCommandBuffer)
    }
}