package VkRender

import View.LocalPlayerView
import VkRender.Descriptors.DescriptorPool
import VkRender.Descriptors.DescriptorSetLayout
import VkRender.Descriptors.DescriptorSets
import VkRender.Surfaces.NativeSurface
import VkRender.Surfaces.Surface
import VkRender.Sync.Fences
import VkRender.Sync.Semaphores
import VkRender.Textures.Images
import VkRender.Textures.Sampler
import VkRender.Textures.TextureImage
import VkRender.buffers.IndexBuffer
import VkRender.buffers.UpdatingUniformBuffer
import VkRender.buffers.VertexBuffer
import VkRender.buffers.VertexStagingBuffer
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.awt.AWTVKCanvas
import org.lwjgl.vulkan.awt.VKData
import java.awt.Color
import java.awt.Graphics
import java.awt.event.*
import java.io.Closeable
import kotlin.system.measureNanoTime

class VkCanvas(private val instance: Instance, val controller: Controller.Controller) : AWTVKCanvas(VKData().also { it.instance = instance.instance }), ComponentListener,
    Closeable {

    lateinit var sfc: Surface
    lateinit var physicalDevice: PhysicalDevice
        private set
    lateinit var device: Device
        private set
    lateinit var renderPass: RenderPass
        private set
    lateinit var swapChain: SwapChain
        private set
    lateinit var descriptorSetLayout: DescriptorSetLayout
        private set
    lateinit var pipeline: GraphicsPipeline
        private set
    lateinit var commands: CommandPool
        private set
    lateinit var inFlightFences: Fences
        private set
    lateinit var imageAvailableSemaphore: Semaphores
    lateinit var renderFinishedSemaphore: Semaphores
    private lateinit var vertexBuffer: VertexBuffer
    private lateinit var indexBuffer: IndexBuffer
    lateinit var updatingUniformBuffer: UpdatingUniformBuffer
    lateinit var textures: Images
    lateinit var sampler: Sampler

    lateinit var descriptorPool: DescriptorPool
    lateinit var descriptorSets: DescriptorSets

    private var currentFrame = 0
    private var framebufferResized = false

    val vertices: List<Vertex> = controller.localPlayerView.vertices

    var indexes: List<Int> = controller.localPlayerView.indexes
        set(value) {
            field = value
            if (this::device.isInitialized) {
                indexBuffer = IndexBuffer(device, physicalDevice, commands, value)
            }
        }

    override fun initVK() {
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                paint(graphics)
                super.mouseDragged(e)
            }
        })
        addMouseWheelListener(object: MouseWheelListener {
            override fun mouseWheelMoved(p0: MouseWheelEvent?) {
                paint(graphics)
            }
        })
        addMouseListener(controller.areMouseAdapter);
        addMouseMotionListener(controller.areMouseAdapter)
        addMouseWheelListener(controller.areMouseAdapter)

        addComponentListener(this)
        sfc = NativeSurface(surface, instance)
        Util()
        physicalDevice = PhysicalDevice(instance, sfc)
        device = Device(physicalDevice)
        renderPass = RenderPass(device, physicalDevice)
        swapChain = SwapChain(device, physicalDevice, sfc, renderPass, size.width, size.height)

        textures = Images(
            "build/resources/main/images/Grass1.png",
            "build/resources/main/images/Structure1.png",
        )
        descriptorSetLayout = DescriptorSetLayout(device, textures)
        pipeline = GraphicsPipeline(device, renderPass, descriptorSetLayout, Vertex.properties)
        commands = CommandPool(device, physicalDevice)

//        vertexBuffer = VertexBuffer(device, physicalDevice, vertices, Vertex.SIZEOF)
        textures.init(device, physicalDevice, commands)
        sampler = Sampler(device, physicalDevice)

        vertexBuffer = VertexBuffer(device, physicalDevice, commands, vertices.size, Vertex.properties)

        indexBuffer = IndexBuffer(device, physicalDevice, commands, indexes)
        updatingUniformBuffer = UpdatingUniformBuffer(device, physicalDevice, Config.MAX_FRAMES_IN_FLIGHT, Float.SIZE_BYTES * 4L)
        descriptorPool = DescriptorPool(device)
        descriptorSets = DescriptorSets(device, descriptorPool, descriptorSetLayout, updatingUniformBuffer, sampler, textures)

        MemoryStack.stackPush().use { stack ->
            inFlightFences = Fences(Config.MAX_FRAMES_IN_FLIGHT, device, stack)

            imageAvailableSemaphore = Semaphores(Config.MAX_FRAMES_IN_FLIGHT, device, stack)

            renderFinishedSemaphore = Semaphores(Config.MAX_FRAMES_IN_FLIGHT, device, stack)
        }
//        vertices = controller.localPlayerView.vertices.toTypedArray()
//        indexes = controller.localPlayerView.indexes.toTypedArray()
        for (frame in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
            updatingUniformBuffer.update(
                frame,
                controller.localPlayerView.camera_rect_tiles.x.toFloat(),
                controller.localPlayerView.camera_rect_tiles.y.toFloat(),
                controller.localPlayerView.camera_rect_tiles.width.toFloat(),
                controller.localPlayerView.camera_rect_tiles.height.toFloat(),
            )
        }
    }

    override fun paintVK() {

        val time = measureNanoTime {
            MemoryStack.stackPush().use { stack ->

                VK13.vkWaitForFences(device.device, inFlightFences[currentFrame], true, Long.MAX_VALUE)

//            vkResetFences(device, inFlightFence)

                var result = KHRSwapchain.vkAcquireNextImageKHR(
                    device.device,
                    swapChain.swapChain,
                    Long.MAX_VALUE,
                    imageAvailableSemaphore[currentFrame],
                    VK13.VK_NULL_HANDLE,
                    Util.ip
                )

                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || framebufferResized) {
                    swapChain.reCreate(size.width, size.height)
                    framebufferResized = false
                    paintVK()
                    return
                } else if (result != VK13.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    throw IllegalStateException("failed to aquire swap chain image!")
                }

                VK13.vkResetFences(device.device, inFlightFences[currentFrame])

                val imageIndex = Util.ip[0]

                VK13.vkResetCommandBuffer(commands.commandBuffer[currentFrame]!!, 0)

                if (controller.isCameraMoving || controller.isCameraScaled) {
                    updatingUniformBuffer.update(
                        currentFrame,
                        controller.localPlayerView.camera_rect_tiles.x.toFloat(),
                        controller.localPlayerView.camera_rect_tiles.y.toFloat(),
                        controller.localPlayerView.camera_rect_tiles.width.toFloat(),
                        controller.localPlayerView.camera_rect_tiles.height.toFloat(),
                    )
                }
                
                vertexBuffer.update(vertices)

                record(currentFrame, imageIndex)

                val lp2 = stack.mallocLong(1)
                val submitInfo = VkSubmitInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pNext(MemoryUtil.NULL)
                    .waitSemaphoreCount(1)
                    .pWaitSemaphores(Util.lp.put(0, imageAvailableSemaphore[currentFrame]))
                    .pWaitDstStageMask(Util.ip.put(0, VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pCommandBuffers(Util.pp.put(0, commands.commandBuffer[currentFrame]!!))
                    .pSignalSemaphores(lp2.put(0, renderFinishedSemaphore[currentFrame]))

                if (VK13.vkQueueSubmit(
                        device.graphicsQueue,
                        submitInfo,
                        inFlightFences[currentFrame]
                    ) != VK13.VK_SUCCESS
                ) {
                    throw IllegalStateException("failed to submit draw command buffer")
                }

                val presentInfo = VkPresentInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(lp2)
                    .swapchainCount(1)
                    .pSwapchains(Util.lp.put(0, swapChain.swapChain))
                    .pImageIndices(Util.ip.put(0, imageIndex))
                    .pResults(null)

                result = KHRSwapchain.vkQueuePresentKHR(device.graphicsQueue, presentInfo)

                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                    framebufferResized = false
                    swapChain.reCreate(size.width, size.height)
                } else if (result != VK13.VK_SUCCESS) {
                    throw IllegalStateException("failed to present swap chain image!")
                }
                currentFrame = (currentFrame + 1) % Config.MAX_FRAMES_IN_FLIGHT

//                val selectionRect = controller.selectionRect
//                if (controller.isSelecting) {
//                    this.graphics.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height)
//                }
            }
        }
//        println("fps: ${1e9 / time}")
    }

    fun record(currentFrame: Int, imageIndex: Int) {
        val currentCommandBuffer = commands.commandBuffer[currentFrame]!!
        MemoryStack.stackPush().use { stack ->
            val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
//                .flags(0)
//                .pInheritanceInfo(null)

            if (VK13.vkBeginCommandBuffer(currentCommandBuffer, beginInfo) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to begin recording command buffer!")
            }

            val clearColor = VkClearValue.calloc(1, stack)
            clearColor.color()
                .float32(0, 0.0f)
                .float32(1, 0.0f)
                .float32(2, 0.0f)
                .float32(3, 1.0f)

            val renderPassInfo = VkRenderPassBeginInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .renderPass(renderPass.renderPass)
                .framebuffer(swapChain.swapChainFramebuffers[imageIndex])
                .renderArea {
                    it.offset {
                        it.x(0)
                            .y(0)
                    }
                    it.extent {
                        it.width(width)
                            .height(height)
                    }
                }
                .clearValueCount(1)
                .pClearValues(clearColor)
//                .pNext(MemoryUtil.NULL)

            VK13.vkCmdBeginRenderPass(currentCommandBuffer, renderPassInfo, VK13.VK_SUBPASS_CONTENTS_INLINE)
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.graphicsPipeLine)

            val viewport = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(width.toFloat())
                .height(height.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewport)

            val scissor = VkRect2D.calloc(1, stack)
                .offset {
                    it.x(0)
                        .y(0)
                }
                .extent {
                    it.width(width)
                        .height(height)
                }

            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissor)

            val vertexBufferptr = stack.longs(vertexBuffer.buffer.vertexBuffer)
            val offsets = stack.longs(0)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, vertexBufferptr, offsets)

            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)

            val currentDescriptorSet = stack.longs(descriptorSets.descriptorSets[currentFrame])

//            val layout = stack.longs(descriptorSetLayout.descriptorSetLayout)
//            VK13.vkCmdDraw(currentCommandBuffer, vertices.size, 1, 0, 0)
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.layout, 0, currentDescriptorSet, null)

            VK13.vkCmdDrawIndexed(currentCommandBuffer, indexes.size, 1, 0, 0, 0)

            VK13.vkCmdEndRenderPass(currentCommandBuffer)
            if (VK13.vkEndCommandBuffer(currentCommandBuffer) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to record command buffer")
            }
            val selectionRect = controller.selectionRect
            if (controller.isSelecting) {
                this.graphics.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height)
            }
        }
    }

    override fun componentResized(p0: ComponentEvent?) {
        framebufferResized = true
    }

    override fun componentMoved(p0: ComponentEvent?) {
    }

    override fun componentShown(p0: ComponentEvent?) {
    }

    override fun componentHidden(p0: ComponentEvent?) {
    }

    override fun close() {
        inFlightFences.close()
        renderFinishedSemaphore.close()
        imageAvailableSemaphore.close()
        commands.close()
        pipeline.close()
        vertexBuffer.close()
        indexBuffer.close()
        updatingUniformBuffer.close()
        renderPass.close()
        swapChain.close()
        device.close()
        physicalDevice.close()
        sfc.close()
//        instance.close()
    }

}