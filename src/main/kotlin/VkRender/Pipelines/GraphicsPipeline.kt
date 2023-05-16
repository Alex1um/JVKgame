package VkRender.Pipelines

import VkRender.Device
import org.lwjgl.vulkan.VK13.vkDestroyPipeline
import org.lwjgl.vulkan.VK13.vkDestroyPipelineLayout
import java.io.Closeable

class GraphicsPipeline(
    val ldevice: Device,
    val layout: Long,
    val graphicsPipeLine: Long,
) : Closeable {
    override fun close() {
        vkDestroyPipeline(ldevice.device, graphicsPipeLine, null)
        vkDestroyPipelineLayout(ldevice.device, layout, null)
    }

}