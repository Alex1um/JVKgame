package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class VertexStagingBuffer(ldevice: Device, physicalDevice: PhysicalDevice, vertices: Array<Vertex>, commands: CommandPool, graphicsQueue: VkQueue) : Closeable {

    val stagingBuffer: VertexBuffer
    val vertexBuffer: VertexBuffer

    init {
        val bufferSize = (vertices.size * Vertex.SIZEOF).toLong()
        stagingBuffer = VertexBuffer(
            ldevice,
            physicalDevice,
            bufferSize,
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT or VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)

        stagingBuffer.fill(vertices)

        vertexBuffer = VertexBuffer(
            ldevice,
            physicalDevice,
            bufferSize,
            VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)

        copyBuffer(ldevice, commands, graphicsQueue)

        stagingBuffer.close()
    }

    private fun copyBuffer(ldevide: Device, commands: CommandPool, graphicsQueue: VkQueue) {
        MemoryStack.stackPush().use { stack ->
            val allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandPool(commands.pool)
                .commandBufferCount(1)

            val pCommandBuffer = stack.pointers(0)
            vkAllocateCommandBuffers(ldevide.device, allocInfo, pCommandBuffer)
            val commandBuffer = VkCommandBuffer(pCommandBuffer[0], ldevide.device)

            val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)

            val beginCommandBuffer = vkBeginCommandBuffer(commandBuffer, beginInfo)

            val copyRegion = VkBufferCopy.calloc(1, stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(stagingBuffer.size)
            vkCmdCopyBuffer(commandBuffer, stagingBuffer.vertexBuffer, vertexBuffer.vertexBuffer, copyRegion)
            vkEndCommandBuffer(commandBuffer)

            val submitInfo = VkSubmitInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pCommandBuffers(pCommandBuffer)

            vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE)
            vkQueueWaitIdle(graphicsQueue)
            vkFreeCommandBuffers(ldevide.device, commands.pool, pCommandBuffer)
        }
    }

    override fun close() {
//        stagingBuffer.close()
        vertexBuffer.close()
    }
}