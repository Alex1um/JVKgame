package VkRender.ShaderModule

import VkRender.Device
import org.lwjgl.vulkan.VK13.VK_SHADER_STAGE_VERTEX_BIT

class VertexShader(ldevice: Device, path: String) : ShaderModule(ldevice, path) {
    override val shaderType: Int = VK_SHADER_STAGE_VERTEX_BIT
}