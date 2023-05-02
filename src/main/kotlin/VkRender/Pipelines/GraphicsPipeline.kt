package VkRender.Pipelines

import VkRender.Descriptors.DescriptorSetLayout
import VkRender.Descriptors.FilledDescriptorSetLayout
import VkRender.Device
import VkRender.GPUObjects.Properties
import VkRender.RenderPass
import VkRender.ShaderModule.ShaderModule
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class GraphicsPipeline(
    val ldevice: Device,
    val graphicsPipeLine: Long,
    val layout: Long
) : Closeable {
    override fun close() {
        vkDestroyPipeline(ldevice.device, graphicsPipeLine, null)
        vkDestroyPipelineLayout(ldevice.device, layout, null)
    }

}