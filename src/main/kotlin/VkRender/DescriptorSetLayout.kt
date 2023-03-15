package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
import java.io.Closeable

class DescriptorSetLayout(val ldevice: Device) : Closeable {

    val descriptorSetLayout: Long

    init {

        MemoryStack.stackPush().use { stack ->

            val ssLayoutBinding = VkDescriptorSetLayoutBinding.calloc(1, stack)
                .binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
                .pImmutableSamplers(null)

            val ssDescriptorSetLayoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pBindings(ssLayoutBinding)


            if (vkCreateDescriptorSetLayout(
                    ldevice.device,
                    ssDescriptorSetLayoutInfo,
                    null,
                    Util.lp
                ) != VK_SUCCESS
            ) {
                throw IllegalStateException("Cannot create descriptor layout")
            }

            descriptorSetLayout = Util.lp[0]

//        val pipelineLayout = VkPipelineLayoutCreateInfo.calloc(stack)
//            .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
//            .pSetLayouts(pDescriptorSetLayouts)
        }
    }

    override fun close() {
        vkDestroyDescriptorSetLayout(ldevice.device, descriptorSetLayout, null)
    }

}