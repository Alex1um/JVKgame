package VkRender.Sync
import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.vkCreateSemaphore
import org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO
import org.lwjgl.vulkan.VK13.vkDestroySemaphore
import org.lwjgl.vulkan.VK13.VK_SUCCESS
import org.lwjgl.vulkan.VkSemaphoreCreateInfo
import java.io.Closeable

class Semaphore(private val ldevice: Device, stack: MemoryStack) : Closeable {

    val semaphore: Long

    init {

        val semaphoreInfo = VkSemaphoreCreateInfo.malloc(stack)
            .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
            .pNext(MemoryUtil.NULL)
            .flags(0)


        if (vkCreateSemaphore(ldevice.device, semaphoreInfo, null, Util.lp) != VK_SUCCESS) {
            throw IllegalStateException("failed to create semaphore!")
        }

        semaphore = Util.lp[0]

    }

    override fun close() {
        vkDestroySemaphore(ldevice.device, semaphore, null)
    }

}
