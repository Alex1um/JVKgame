package VkRender.Descriptors

import VkRender.Config
import VkRender.Device
import VkRender.Textures.Images
import VkRender.Textures.Sampler
import VkRender.buffers.UpdatingUniformBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorBufferInfo
import org.lwjgl.vulkan.VkDescriptorImageInfo
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo
import org.lwjgl.vulkan.VkWriteDescriptorSet
import java.io.Closeable

class DescriptorSets(
    val ldevice: Device,
    val descriptorPool: DescriptorPool,
    val descriptorSetLayout: FilledDescriptorSetLayout,
    ssb: UpdatingUniformBuffer? = null,
    sampler: Sampler? = null,
    textures: Images? = null,
) : Closeable {

    val descriptorSets = MemoryUtil.memCallocLong(Config.MAX_FRAMES_IN_FLIGHT)
    init {
        MemoryStack.stackPush().use { stack ->

            val layouts = stack.callocLong(Config.MAX_FRAMES_IN_FLIGHT)
            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                layouts.put(i, descriptorSetLayout.descriptorSetLayout)
            }

            // Descriptor sets
            val allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool.descriptorPool)
                .pSetLayouts(layouts)

            if (vkAllocateDescriptorSets(ldevice.device, allocInfo, descriptorSets) != VK_SUCCESS) {
                throw IllegalStateException("Cannot allocate descriptor sets")
            }

            val descriptorWriteCount = arrayOf(ssb != null, sampler != null, textures != null).count { it }
            val descriptorWrite = VkWriteDescriptorSet.calloc(descriptorWriteCount, stack)

            // Configure
            if (ssb != null) {
                val bufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                    .offset(0)
                    .range(ssb.el_size)
                val descriptorWritessb = descriptorWrite[0]
                descriptorWritessb
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(0)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    // array elems
                    .descriptorCount(1)
                    .pBufferInfo(bufferInfo)
                    .pImageInfo(null)
                    .pTexelBufferView(null)

                for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                    bufferInfo.buffer(ssb.buffers[i].vertexBuffer)
                }
            }

            if (sampler != null) {
                val samplerInfo = VkDescriptorImageInfo.calloc(1, stack)
                    .sampler(sampler.sampler)

                val descriptorWriteSampler = descriptorWrite[1]
                descriptorWriteSampler
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(1)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(samplerInfo)

            }

            if (textures != null) {

                val imageInfo = VkDescriptorImageInfo.calloc(textures.size, stack)
                for ((i: Int, texture) in textures.textures.withIndex()) {
                    imageInfo[i].imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(texture.view.view)
                        .sampler(0)
                }

                val descriptorWriteImages = descriptorWrite[2]
                descriptorWriteImages
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(2)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE)
                    .descriptorCount(textures.size)
                    .pImageInfo(imageInfo)

            }

            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                for (j in 0 until descriptorWriteCount) {
                    descriptorWrite[j].dstSet(descriptorSets[i])
                }
                vkUpdateDescriptorSets(ldevice.device, descriptorWrite, null)
            }
        }
    }

    override fun close() {
        MemoryUtil.memFree(descriptorSets)
    }
}