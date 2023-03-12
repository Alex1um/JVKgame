package VkRender

import VkRender.Surfaces.NativeSurface
import VkRender.Surfaces.Surface
import VkRender.Sync.Fences
import VkRender.Sync.Semaphore
import VkRender.Sync.Semaphores
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkPresentInfoKHR
import org.lwjgl.vulkan.VkSubmitInfo
import org.lwjgl.vulkan.awt.AWTVKCanvas
import org.lwjgl.vulkan.awt.VKData
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.Closeable

class VkCanvas(private val instance: Instance) : AWTVKCanvas(VKData().also { it.instance = instance.instance }), ComponentListener,
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
    lateinit var pipeline: GraphicsPipeline
        private set
    lateinit var commands: CommandPool
        private set
    lateinit var inFlightFences: Fences
        private set
    lateinit var imageAvailableSemaphore: Semaphores
    lateinit var renderFinishedSemaphore: Semaphores
    lateinit var vertexBuffer: VertexBuffer

    private var currentFrame = 0
    private var framebufferResized = false

    private val vertices = arrayOf(
        Vertex(Vector2f(-0.5f, 0.5f), Vector3f(1f, 0f, 0f)),
        Vertex(Vector2f(0f, -0.5f), Vector3f(1f, 1f, 0f)),
        Vertex(Vector2f(0.5f, 0.5f), Vector3f(0f, 1f, 0f)),
        Vertex(Vector2f(-0f, 0.8f), Vector3f(1f, 0f, 0f)),
        Vertex(Vector2f(0f, -1f), Vector3f(1f, 1f, 0f)),
        Vertex(Vector2f(0.5f, 0.5f), Vector3f(0f, 1f, 0f)),
    )

    override fun initVK() {
        addComponentListener(this)
        sfc = NativeSurface(surface, instance)
        Util()
        physicalDevice = PhysicalDevice(instance, sfc)
        device = Device(physicalDevice)
        renderPass = RenderPass(device, physicalDevice)
        swapChain = SwapChain(device, physicalDevice, sfc, renderPass, size.width, size.height)
        pipeline = GraphicsPipeline(device, renderPass, size.width, size.height)
        commands = CommandPool(device, physicalDevice)

//        vertexBuffer = VertexBuffer(device, physicalDevice, vertices, Vertex.SIZEOF)
        vertexBuffer = VertexStagingBuffer(device, physicalDevice, vertices, commands, device.graphicsQueue).vertexBuffer

        MemoryStack.stackPush().use { stack ->
            inFlightFences = Fences(2, device, stack)

            imageAvailableSemaphore = Semaphores(2, device, stack)

            renderFinishedSemaphore = Semaphores(2, device, stack)
        }
    }

    override fun paintVK() {

        println(1)

        var imageIndex = 0
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

            imageIndex = Util.ip[0]

            VK13.vkResetCommandBuffer(commands.commandBuffer[currentFrame]!!, 0)

            commands.record(currentFrame, imageIndex, renderPass, pipeline, swapChain, size.width, size.height, vertexBuffer, vertices.size)

            val lp2 = stack.mallocLong(1)
            val submitInfo = VkSubmitInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(MemoryUtil.NULL)
                .waitSemaphoreCount(1)
                .pWaitSemaphores(Util.lp.put(0, imageAvailableSemaphore[currentFrame]))
                .pWaitDstStageMask(Util.ip.put(0, VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(Util.pp.put(0, commands.commandBuffer[currentFrame]!!))
                .pSignalSemaphores(lp2.put(0, renderFinishedSemaphore[currentFrame]))

            if (VK13.vkQueueSubmit(device.graphicsQueue, submitInfo, inFlightFences[currentFrame]) != VK13.VK_SUCCESS) {
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
        renderPass.close()
        swapChain.close()
        device.close()
        physicalDevice.close()
        sfc.close()
//        instance.close()
    }


}