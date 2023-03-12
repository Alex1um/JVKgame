package VkRender

import org.lwjgl.system.MemoryStack
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
                throw IllegalStateException("failed to allocate command buffers!")
            }
            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                commandBuffer[i] = VkCommandBuffer(pp2[i], ldevice.device)
            }
        }
    }

    override fun close() {
        VK13.vkDestroyCommandPool(ldevice.device, pool, null)
    }

}