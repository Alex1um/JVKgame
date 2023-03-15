package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VkDescriptorPoolSize
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDescriptorBufferInfo
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo
import org.lwjgl.vulkan.VkWriteDescriptorSet
import java.io.Closeable

class UniformDescriptors(val ldevice: Device, descriptorSetLayout: DescriptorSetLayout, ssb: SquareSizeBuffer) : Closeable {

    val descriptorPool: Long
    val descriptorSets = MemoryUtil.memCallocLong(Config.MAX_FRAMES_IN_FLIGHT)

    init {
        MemoryStack.stackPush().use { stack ->
            val poolSize = VkDescriptorPoolSize.calloc(1, stack)
                .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(Config.MAX_FRAMES_IN_FLIGHT)

            val poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSize)
                .maxSets(Config.MAX_FRAMES_IN_FLIGHT)

            if (vkCreateDescriptorPool(ldevice.device, poolInfo, null, Util.lp) != VK_SUCCESS) {
                throw IllegalStateException("Cannot create descriptor pool")
            }

            descriptorPool = Util.lp[0]

            val layouts = stack.callocLong(Config.MAX_FRAMES_IN_FLIGHT)
            for (i in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
                layouts.put(i, descriptorSetLayout.descriptorSetLayout)
            }
//            val layouts = stack.longs(Config.MAX_FRAMES_IN_FLIGHT.toLong(), descriptorSetLayout.descriptorSetLayout)

            // Descriptor sets
            val allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool)
                .pSetLayouts(layouts)

            if (vkAllocateDescriptorSets(ldevice.device, allocInfo, descriptorSets) != VK_SUCCESS) {
                throw IllegalStateException("Cannot allocate descriptor sets")
            }

            // Configure
            val bufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                .offset(0)
                .range(ssb.size)

            val descriptorWrite = VkWriteDescriptorSet.calloc(1, stack)
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

                descriptorWrite.dstSet(descriptorSets[i])

                vkUpdateDescriptorSets(ldevice.device, descriptorWrite, null)
            }


        }
    }

    override fun close() {
        vkDestroyDescriptorPool(ldevice.device, descriptorPool, null)
        MemoryUtil.memFree(descriptorSets)
    }
}