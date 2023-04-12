package VkRender.Descriptors

import VkRender.Config
import VkRender.Device
import VkRender.Textures.TextureImage
import VkRender.buffers.UpdatingUniformBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorBufferInfo
import org.lwjgl.vulkan.VkDescriptorImageInfo
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo
import org.lwjgl.vulkan.VkWriteDescriptorSet
import java.io.Closeable

class DescriptorSets(val ldevice: Device, descriptorPool: DescriptorPool, descriptorSetLayout: DescriptorSetLayout, ssb: UpdatingUniformBuffer, texture: TextureImage) : Closeable {

    val descriptorSets = MemoryUtil.memCallocLong(Config.MAX_FRAMES_IN_FLIGHT)

    init {
        MemoryStack.stackPush().use { stack ->

            val layouts = stack.callocLong(Config.MAX_FRAMES_IN_FLIGHT)
            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                layouts.put(i, descriptorSetLayout.descriptorSetLayout)
            }
//            val layouts = stack.longs(Config.MAX_FRAMES_IN_FLIGHT.toLong(), descriptorSetLayout.descriptorSetLayout)

            // Descriptor sets
            val allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool.descriptorPool)
                .pSetLayouts(layouts)

            if (vkAllocateDescriptorSets(ldevice.device, allocInfo, descriptorSets) != VK_SUCCESS) {
                throw IllegalStateException("Cannot allocate descriptor sets")
            }

            // Configure
            val bufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                .offset(0)
                .range(ssb.size)

            val imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                .imageView(texture.view.view)
                .sampler(texture.sampler.sampler)

            val descriptorWrite = VkWriteDescriptorSet.calloc(2, stack)
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

            val descriptorWriteSampler = descriptorWrite[1]
            descriptorWriteSampler
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstBinding(1)
                .dstArrayElement(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1)
                .pImageInfo(imageInfo)

            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                bufferInfo.buffer(ssb.buffers[i].vertexBuffer)

                descriptorWrite.dstSet(descriptorSets[i])

                descriptorWriteSampler.dstSet(descriptorSets[i])

                vkUpdateDescriptorSets(ldevice.device, descriptorWrite, null)
            }
        }
    }

    override fun close() {
        MemoryUtil.memFree(descriptorSets)
    }
}