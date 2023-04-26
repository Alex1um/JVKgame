package VkRender.ShaderModule

import VkRender.Device
import org.lwjgl.vulkan.VK13.VK_SHADER_STAGE_FRAGMENT_BIT

class FragmentShader(ldevice: Device, path: String) : ShaderModule(ldevice, path) {
    override val shaderType: Int = VK_SHADER_STAGE_FRAGMENT_BIT
}