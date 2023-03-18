package VkRender.Textures

import VkRender.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class Image(stack: MemoryStack,
            val ldevice: Device,
            physicalDevice: PhysicalDevice,
            width: Int,
            height: Int,
            format: Int,
            tiling: Int,
            usage: Int,
            properties: Int) : Closeable {

    val imageh: Long
    val memory: Long

    init {
        val imageInfo = VkImageCreateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
            .imageType(VK_IMAGE_TYPE_2D)
            .extent {
                with(it) {
                    width(width)
                    height(height)
                    depth(1)
                }
            }
            .mipLevels(1)
            .arrayLayers(1)
            .format(format)
            .tiling(tiling)
            .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            .usage(usage)
            .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
            .samples(VK_SAMPLE_COUNT_1_BIT)
            .flags(0)

        if (vkCreateImage(ldevice.device, imageInfo, null, Util.lp) != VK_SUCCESS) {
            throw IllegalStateException("Failed to create image")
        }

        imageh = Util.lp[0]

        val requirements = VkMemoryRequirements.calloc(stack)
        vkGetImageMemoryRequirements(ldevice.device, imageh, requirements)

        val allocInfo = VkMemoryAllocateInfo.calloc(stack)
            .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
            .allocationSize(requirements.size())
            .memoryTypeIndex(
                Util.findMemoryType(
                    stack, physicalDevice, requirements.memoryTypeBits(),
                    properties
                )
            )

        if (vkAllocateMemory(ldevice.device, allocInfo, null, Util.lp) != VK_SUCCESS) {
            throw IllegalStateException("Failed to allocate Memory")
        }

        memory = Util.lp[0]

        vkBindImageMemory(ldevice.device, imageh, memory, 0)
    }

    fun transitionImageLayout(commands: CommandPool, format: Int, oldLayout: Int, newLayout: Int) {
        MemoryStack.stackPush().use { stack ->
            SingleTimeCommands(stack, ldevice, commands).use {

                val barrier = VkImageMemoryBarrier.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .oldLayout(oldLayout)
                    .newLayout(newLayout)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .image(this.imageh)
                    .subresourceRange {
                        with(it) {
                            aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                            baseMipLevel(0)
                            levelCount(1)
                            baseArrayLayer(0)
                            layerCount(1)
                        }
                    }

                val sourceStage: Int
                val destinationStage: Int

                if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                    barrier
                        .srcAccessMask(0)
                        .dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)

                    sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT
                    destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT
                } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                    barrier
                        .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                        .dstAccessMask(VK_ACCESS_SHADER_READ_BIT)

                    sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT
                    destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
                } else {
                    throw IllegalStateException("Unsupported layout transition")
                }

                vkCmdPipelineBarrier(it.commandBuffer, sourceStage, destinationStage,0, null, null, barrier)
            }
        }
    }

    override fun close() {
        vkDestroyImage(ldevice.device, imageh, null)
        vkFreeMemory(ldevice.device, memory, null)
    }
}