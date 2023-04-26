package VkRender.ShaderModule

import VkRender.Device
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.vkDestroyShaderModule
import org.lwjgl.vulkan.VK13.VK_SUCCESS
import org.lwjgl.vulkan.VK13.vkCreateShaderModule
import org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO
import org.lwjgl.vulkan.VkShaderModuleCreateInfo
import java.io.Closeable

abstract class ShaderModule(private val ldevice: Device, data: ByteArray) : Closeable {
    val module: Long

    abstract val shaderType: Int

    constructor(ldevice: Device, path: String) : this(ldevice, Util.readFile(path))

    init {
        MemoryStack.stackPush().use { stack ->
            val pCode = MemoryUtil.memAlloc(data.size).put(data)
            pCode.flip()
            val createInfo = VkShaderModuleCreateInfo.malloc(stack)
                .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(pCode)
                .flags(0)
                .pNext(MemoryUtil.NULL)
            val rt = vkCreateShaderModule(ldevice.device, createInfo, null, Util.lp)
            if (rt != VK_SUCCESS) {
                throw IllegalStateException("failed to create shader module!")
            }
            MemoryUtil.memFree(pCode)
            module = Util.lp[0]
        }
    }

    override fun close() {
        vkDestroyShaderModule(ldevice.device, module, null)
    }

}
