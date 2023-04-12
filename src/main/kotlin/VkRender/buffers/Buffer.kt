package VkRender.buffers

import VkRender.*
import VkRender.Textures.Image
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
                .memoryTypeIndex(
                    Util.findMemoryType(
                        stack,
                        physicalDevice,
                        memoryRequirements.memoryTypeBits(),
                        properties
                    )
                )

            if (vkAllocateMemory(ldevide.device, memoryAllocateInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Failed to allocate memory for vertex buffer")
            }

            vertexBufferMemory = Util.lp[0]

            vkBindBufferMemory(ldevide.device, vertexBuffer, vertexBufferMemory, 0)

        }
    }

    fun fillVertices(vertixes: List<Vertex>) {

        fun memcpy(buffer: ByteBuffer, array: List<Vertex>) {

            for (v in array) {
                v.put(buffer)
            }
        }

        vkMapMemory(ldevide.device, vertexBufferMemory, 0, size, 0, Util.pp)
        memcpy(Util.pp.getByteBuffer(0, size.toInt()), vertixes)
        vkUnmapMemory(ldevide.device, vertexBufferMemory)
    }

    fun fill(vararg ints: Int) {

        fun memcpy(buffer: ByteBuffer, vararg ints: Int) {

            for (e in ints) {
                buffer.putFloat(e.toFloat())
            }
        }

        vkMapMemory(ldevide.device, vertexBufferMemory, 0, size, 0, Util.pp)
        memcpy(Util.pp.getByteBuffer(0, size.toInt()), *ints)
        vkUnmapMemory(ldevide.device, vertexBufferMemory)
    }

    fun fillIndices(indexes: List<Int>) {

        fun memcpy(buffer: ByteBuffer, array: List<Int>) {

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


    fun fill(src: ByteBuffer, size: Int) {

        vkMapMemory(ldevide.device, vertexBufferMemory, 0, size.toLong(), 0, Util.pp)
        val dest = Util.pp.getByteBuffer(0, size)
        src.limit(size)
        dest.put(src)
        src.limit(src.capacity()).rewind()
        vkUnmapMemory(ldevide.device, vertexBufferMemory)
    }

    constructor(
        ldevide: Device,
        physicalDevice: PhysicalDevice,
        vertixes: List<Vertex>) : this(
        ldevide,
        physicalDevice,
        (vertixes.size * Vertex.properties.SIZEOF).toLong(),
        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) {
            this.fillVertices(vertixes)
        }

    fun copyBuffer(ldevide: Device, commands: CommandPool, dest: Buffer) {
        MemoryStack.stackPush().use { stack ->
            SingleTimeCommands(stack, ldevide, commands).use {

                val copyRegion = VkBufferCopy.calloc(1, stack)
                    .srcOffset(0)
                    .dstOffset(0)
                    .size(this.size)
                vkCmdCopyBuffer(it.commandBuffer, this.vertexBuffer, dest.vertexBuffer, copyRegion)

            }
        }
    }

    fun copyToImage(stack: MemoryStack, ldevice: Device, commands: CommandPool, width: Int, height: Int, image: Image) {
        SingleTimeCommands(stack, ldevice, commands).use {
            val region = VkBufferImageCopy.calloc(1, stack)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                .imageSubresource {
                    with(it) {
                        aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                        mipLevel(0)
                        baseArrayLayer(0)
                        layerCount(1)
                    }
                }
                .imageOffset {
                    with(it) {
                        x(0)
                        y(0)
                        z(0)
                    }
                }
                .imageExtent {
                    it.width(width)
                    it.height(height)
                    it.depth(1)
                }

            vkCmdCopyBufferToImage(it.commandBuffer, vertexBuffer, image.imageh, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region)
        }
    }
    override fun close() {
        vkDestroyBuffer(ldevide.device, vertexBuffer, null)
        vkFreeMemory(ldevide.device, vertexBufferMemory, null)
    }

}