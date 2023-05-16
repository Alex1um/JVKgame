package VkRender.Descriptors

import VkRender.Device
import VkRender.Textures.Images
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo
import java.nio.LongBuffer

class DescriptorSetLayout(val ldevice: Device) {

    val descriptorSetLayoutBindings: MutableList<Binding> = mutableListOf()

    open inner class Binding(val type: Int, val descriptorCount: Int, val stageFlags: Int, val pImmutableSamples: LongBuffer?)
    inner class UniformsBinding( descriptorCount: Int,  stageFlags: Int,  pImmutableSamples: LongBuffer?) :
        Binding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, descriptorCount, stageFlags, pImmutableSamples)
    inner class SamplersBinding( descriptorCount: Int,  stageFlags: Int,  pImmutableSamples: LongBuffer?) :
        Binding(VK_DESCRIPTOR_TYPE_SAMPLER, descriptorCount, stageFlags, pImmutableSamples)
    inner class SampledImagesBinding( descriptorCount: Int,  stageFlags: Int,  pImmutableSamples: LongBuffer?) :
        Binding(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE, descriptorCount, stageFlags, pImmutableSamples)

    fun addUniforms(count: Int = 1, stageFlags: Int = VK_SHADER_STAGE_VERTEX_BIT, pImmutableSamples: LongBuffer? = null): DescriptorSetLayout {
        descriptorSetLayoutBindings.add(
            UniformsBinding(count, stageFlags, pImmutableSamples)
        )
        return this
    }

    fun addSampledTextures(
        textures: Images,
        stageFlags: Int = VK_SHADER_STAGE_VERTEX_BIT,
        pImmutableSamples: LongBuffer? = null,
    ): DescriptorSetLayout {
        descriptorSetLayoutBindings.add(
            SampledImagesBinding(textures.size, stageFlags, pImmutableSamples)
        )
        return this
    }

    fun addSamplers(
        count: Int = 1,
        stageFlags: Int = VK_SHADER_STAGE_VERTEX_BIT,
        pImmutableSamples: LongBuffer? = null,
    ): DescriptorSetLayout {
        descriptorSetLayoutBindings.add(
            SamplersBinding(count, stageFlags, pImmutableSamples)
        )
        return this
    }

    fun done(): FilledDescriptorSetLayout {

        val descriptorSetLayout: Long

        MemoryStack.stackPush().use { stack ->

            val ssLayoutBinding = VkDescriptorSetLayoutBinding.calloc(descriptorSetLayoutBindings.size, stack)
            for ((i, binding) in descriptorSetLayoutBindings.withIndex()) {
                ssLayoutBinding[i]
                    .binding(i)
                    .descriptorCount(binding.descriptorCount)
                    .pImmutableSamplers(binding.pImmutableSamples)
                    .stageFlags(binding.stageFlags)
                    .descriptorType(binding.type)

            }

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

        return FilledDescriptorSetLayout(ldevice, descriptorSetLayout, descriptorSetLayoutBindings)
    }

}