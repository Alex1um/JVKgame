package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable
import java.nio.ByteBuffer

class Buffer(
    private val ldevide: Device,
    physicalDevice: PhysicalDevice,
    val size: Long,
    usage: Int,
    properties: Int
) : Closeable {

    val vertexBuffer: Long
    val vertexBufferMemory: Long

    init {
        MemoryStack.stackPush().use { stack ->
            val bufferInfo = VkBufferCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(size)
                .usage(usage)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE)

            if (vkCreateBuffer(ldevide.device, bufferInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Cannot create vertex buffer")
            }

            vertexBuffer = Util.lp[0]

            val memoryRequirements = VkMemoryRequirements.calloc(stack)
            vkGetBufferMemoryRequirements(ldevide.device, vertexBuffer, memoryRequirements)

            val memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(memoryRequirements.size())
                .memoryTypeIndex(Util.findMemoryType(
                    stack,
                    physicalDevice,
                    memoryRequirements.memoryTypeBits(),
                    properties))

            if (vkAllocateMemory(ldevide.device, memoryAllocateInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Failed to allocate memory for vertex buffer")
            }

            vertexBufferMemory = Util.lp[0]

            vkBindBufferMemory(ldevide.device, vertexBuffer, vertexBufferMemory, 0)

        }
    }

    fun fill(vertixes: Array<Vertex>) {

        fun memcpy(buffer: ByteBuffer, array: Array<Vertex>) {

            fun ByteBuffer.put(v: Vertex) {
                this.putFloat(v.pos.x())
                this.putFloat(v.pos.y())

                this.putFloat(v.color.x())
                this.putFloat(v.color.y())
                this.putFloat(v.color.z())
            }

            for (e in array) {
                buffer.put(e)
            }
        }

        vkMapMemory(ldevide.device, vertexBufferMemory, 0, size, 0, Util.pp)
        memcpy(Util.pp.getByteBuffer(0, size.toInt()), vertixes)
        vkUnmapMemory(ldevide.device, vertexBufferMemory)
    }

    fun fill(indexes: Array<Int>) {

        fun memcpy(buffer: ByteBuffer, array: Array<Int>) {

            fun ByteBuffer.put(v: Int) {
               this.putInt(v)
            }

            for (e in array) {
                buffer.put(e)
            }
        }

        vkMapMemory(ldevide.device, vertexBufferMemory, 0, size, 0, Util.pp)
        memcpy(Util.pp.getByteBuffer(0, size.toInt()), indexes)
        vkUnmapMemory(ldevide.device, vertexBufferMemory)
    }

    constructor(
        ldevide: Device,
        physicalDevice: PhysicalDevice,
        vertixes: Array<Vertex>) : this(
        ldevide,
        physicalDevice,
        (vertixes.size * Vertex.SIZEOF).toLong(),
        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) {
            this.fill(vertixes)
        }

    fun copyBuffer(ldevide: Device, commands: CommandPool, graphicsQueue: VkQueue, dest: Buffer) {
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

            vkBeginCommandBuffer(commandBuffer, beginInfo)

            val copyRegion = VkBufferCopy.calloc(1, stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(this.size)
            vkCmdCopyBuffer(commandBuffer, this.vertexBuffer, dest.vertexBuffer, copyRegion)
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
        vkDestroyBuffer(ldevide.device, vertexBuffer, null)
        vkFreeMemory(ldevide.device, vertexBufferMemory, null)
    }

}