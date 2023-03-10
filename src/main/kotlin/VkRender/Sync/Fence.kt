package VkRender.Sync

import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkFenceCreateInfo
import java.io.Closeable

class Fence(private val ldevice: Device) : Closeable {

    val fence: Long

    init {

        MemoryStack.stackPush().use { stack ->

            val fenceInfo = VkFenceCreateInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                .flags(VK13.VK_FENCE_CREATE_SIGNALED_BIT)
                .pNext(MemoryUtil.NULL)


            if (VK13.vkCreateFence(ldevice.device, fenceInfo, null, Util.lp) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create fence!")
            }

            fence = Util.lp[0]
        }

    }

    override fun close() {
        VK13.vkDestroyFence(ldevice.device, fence, null)
    }

}