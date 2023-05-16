package VkRender

import View.LocalPlayerView
import VkRender.Descriptors.DescriptorPool
import VkRender.Descriptors.DescriptorSetLayout
import VkRender.Descriptors.DescriptorSets
import VkRender.Descriptors.FilledDescriptorSetLayout
import VkRender.GPUObjects.GameMapVertex
import VkRender.GPUObjects.HealthBarVertex
import VkRender.GPUObjects.UIVertex
import VkRender.Pipelines.GraphicsPipeline
import VkRender.Pipelines.GraphicsPipelineCreator
import VkRender.ShaderModule.FragmentShader
import VkRender.ShaderModule.VertexShader
import VkRender.Surfaces.NativeSurface
import VkRender.Surfaces.Surface
import VkRender.Sync.Fences
import VkRender.Sync.Semaphores
import VkRender.TextureTable.getPaths
import VkRender.Textures.Images
import VkRender.Textures.Sampler
import VkRender.buffers.IndexBuffer
import VkRender.buffers.UpdatingUniformBuffer
import VkRender.buffers.VertexBuffer
import VkRender.buffers.VertexStagingBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.awt.AWTVKCanvas
import org.lwjgl.vulkan.awt.VKData
import java.awt.Graphics
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.Closeable
import kotlin.system.measureNanoTime

class VkCanvas(private val instance: Instance, val localPlayerView: LocalPlayerView) : AWTVKCanvas(VKData().also { it.instance = instance.instance }), ComponentListener,
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
    lateinit var commands: CommandPool
        private set
    lateinit var inFlightFences: Fences
        private set
    lateinit var imageAvailableSemaphore: Semaphores
    lateinit var renderFinishedSemaphore: Semaphores

    lateinit var descriptorSetLayout: FilledDescriptorSetLayout
        private set
    lateinit var descriptorPool: DescriptorPool
    lateinit var pipeline: GraphicsPipeline
        private set
    lateinit var miniMapPipeline: GraphicsPipeline
        private set
    private lateinit var vertexBuffer: VertexStagingBuffer
    private lateinit var indexBuffer: IndexBuffer
    lateinit var updatingUniformBuffer: UpdatingUniformBuffer
    lateinit var miniMapUpdatingUniformBuffer: UpdatingUniformBuffer
    lateinit var textures: Images
    lateinit var sampler: Sampler

    lateinit var descriptorSets: DescriptorSets
    lateinit var miniMapDescriptorSets: DescriptorSets

    //objectts
    lateinit var objDescriptorSetLayout: FilledDescriptorSetLayout
        private set
    lateinit var objDescriptorPool: DescriptorPool
    lateinit var objPipeline: GraphicsPipeline
        private set
    lateinit var objMiniMapPipeline: GraphicsPipeline
        private set
    private lateinit var objVertexBuffer: VertexBuffer
    lateinit var objTextures: Images
    lateinit var objSampler: Sampler

    lateinit var objDescriptorSets: DescriptorSets
    lateinit var miniMapObjDescriptorSets: DescriptorSets

    // healthBars
    lateinit var healthBarsDescriptorSetLayout: FilledDescriptorSetLayout
        private set
    lateinit var healthBarsDescriptorPool: DescriptorPool
    lateinit var healthBarsPipeline: GraphicsPipeline
        private set
    private lateinit var healthBarsVertexBuffer: VertexBuffer

    lateinit var healthBarsDescriptorSets: DescriptorSets

    //ui
    lateinit var UIdescriptorSetLayout: FilledDescriptorSetLayout
        private set
    lateinit var UIdescriptorPool: DescriptorPool
    lateinit var UIpipeline: GraphicsPipeline
    lateinit var UIvertexBuffer: VertexBuffer
    lateinit var UIindexBuffer: IndexBuffer
    lateinit var UIupdatingUniformBuffer: UpdatingUniformBuffer
    lateinit var UIdescriptorSets: DescriptorSets

    private var currentFrame = 0
    private var framebufferResized = false
    private var ticksSinceStart = 0

    val miniMapWidth: Int
        get() {
            return height / 3
        }

    val miniMapHeight: Int
        get() {
            return height / 3
        }
    override fun initVK() {

        addComponentListener(this)
        sfc = NativeSurface(surface, instance)
        Util()
        physicalDevice = PhysicalDevice(instance, sfc)
        device = Device(physicalDevice)
        renderPass = RenderPass(device, physicalDevice)
        swapChain = SwapChain(device, physicalDevice, sfc, renderPass, size.width, size.height)
        commands = CommandPool(device, physicalDevice)

        textures = Images(
            *TextureTable.tiles.getPaths(),
        )
        descriptorSetLayout = DescriptorSetLayout(device)
            .addUniforms()
            .addSamplers(stageFlags = VK13.VK_SHADER_STAGE_FRAGMENT_BIT)
            .addSampledTextures(textures,stageFlags = VK13.VK_SHADER_STAGE_FRAGMENT_BIT)
            .done()
        descriptorPool = descriptorSetLayout.getPool(copyCount = 2)

        pipeline = GraphicsPipelineCreator()
            .fillDefault()
            .makeViewPortAndScissorDynamicStates()
            .create(device, renderPass, GameMapVertex.properties, descriptorSetLayout,
            VertexShader(device, "build/resources/main/shaders/gameMap.vert.spv"),
            FragmentShader(device, "build/resources/main/shaders/gameMap.frag.spv")
            )
        // todo: make inheritance
        miniMapPipeline = GraphicsPipelineCreator()
            .fillDefault()
            .makeViewPortAndScissorDynamicStates()
            .create(device, renderPass, GameMapVertex.properties, descriptorSetLayout,
                VertexShader(device, "build/resources/main/shaders/gameMap.vert.spv"),
                FragmentShader(device, "build/resources/main/shaders/gameMap.frag.spv"),
                subpass = 1,
            )
//            .create(device, pipeline, GameMapVertex.properties,
//                VertexShader(device, "build/resources/main/shaders/gameMap.vert.spv"),
//                FragmentShader(device, "build/resources/main/shaders/gameMap.frag.spv"),
//                subpass = 1,
//                )

        textures.init(device, physicalDevice, commands)
        sampler = Sampler(device, physicalDevice)
        updatingUniformBuffer = UpdatingUniformBuffer(device, physicalDevice, Config.MAX_FRAMES_IN_FLIGHT, Float.SIZE_BYTES * 6L)
        miniMapUpdatingUniformBuffer = UpdatingUniformBuffer(device, physicalDevice, Config.MAX_FRAMES_IN_FLIGHT, Float.SIZE_BYTES * 3L)

        vertexBuffer = VertexStagingBuffer(device, physicalDevice, localPlayerView.gameMapView.vertices, commands)
        indexBuffer = IndexBuffer(device, physicalDevice, commands, localPlayerView.mapIndexes)
        descriptorSets = DescriptorSets(device, descriptorPool, descriptorSetLayout, updatingUniformBuffer, sampler, textures)
        miniMapDescriptorSets = DescriptorSets(device, descriptorPool, descriptorSetLayout, miniMapUpdatingUniformBuffer, sampler, textures)

        //objects
        objTextures = Images(
            *TextureTable.objects.getPaths(),
        )
        objDescriptorSetLayout = DescriptorSetLayout(device)
            .addUniforms()
            .addSamplers(stageFlags = VK13.VK_SHADER_STAGE_FRAGMENT_BIT)
            .addSampledTextures(objTextures,stageFlags = VK13.VK_SHADER_STAGE_FRAGMENT_BIT)
            .done()
        objDescriptorPool = objDescriptorSetLayout.getPool(copyCount = 2)

        objPipeline = GraphicsPipelineCreator()
            .fillDefault()
            .makeViewPortAndScissorDynamicStates()
            .create(device, renderPass, GameMapVertex.properties, objDescriptorSetLayout,
            VertexShader(device, "build/resources/main/shaders/objects.vert.spv"),
            FragmentShader(device, "build/resources/main/shaders/objects.frag.spv")
        )

        objMiniMapPipeline = GraphicsPipelineCreator()
            .makeViewPortAndScissorDynamicStates()
            .fillDefault()
            .create(device, renderPass, GameMapVertex.properties, objDescriptorSetLayout,
                VertexShader(device, "build/resources/main/shaders/objects.vert.spv"),
                FragmentShader(device, "build/resources/main/shaders/objects.frag.spv"),
                subpass = 1,
            )

        objTextures.init(device, physicalDevice, commands)
        objSampler = Sampler(device, physicalDevice)

        objVertexBuffer = VertexBuffer(device, physicalDevice, localPlayerView.gameMapView.vertices.size, GameMapVertex.properties)
        objDescriptorSets = DescriptorSets(device, objDescriptorPool, objDescriptorSetLayout, updatingUniformBuffer, objSampler, objTextures)
        miniMapObjDescriptorSets = DescriptorSets(device, objDescriptorPool, objDescriptorSetLayout, miniMapUpdatingUniformBuffer, objSampler, objTextures)

        // healthbar

        healthBarsDescriptorSetLayout = DescriptorSetLayout(device)
            .addUniforms(stageFlags = VK13.VK_SHADER_STAGE_FRAGMENT_BIT or VK13.VK_SHADER_STAGE_VERTEX_BIT)
            .done()
        healthBarsDescriptorPool = healthBarsDescriptorSetLayout.getPool()
        healthBarsPipeline = GraphicsPipelineCreator()
            .fillDefault()
            .makeViewPortAndScissorDynamicStates()
            .create(device, renderPass, HealthBarVertex.properties, healthBarsDescriptorSetLayout,
                VertexShader(device, "build/resources/main/shaders/healthBars.vert.spv"),
                FragmentShader(device, "build/resources/main/shaders/healthBars.frag.spv"),
                )
        healthBarsVertexBuffer = VertexBuffer(device, physicalDevice, localPlayerView.gameMapView.vertices.size, HealthBarVertex.properties)
        healthBarsDescriptorSets = DescriptorSets(device, healthBarsDescriptorPool, healthBarsDescriptorSetLayout, updatingUniformBuffer)

        // ui
        UIdescriptorSetLayout = DescriptorSetLayout(device)
            .addUniforms()
            .done()

        UIdescriptorPool = UIdescriptorSetLayout.getPool()

        val UIpipelineCreator = GraphicsPipelineCreator()
            .fillDefault()
            .makeViewPortAndScissorDynamicStates()

        UIpipelineCreator.colorBlendAttachment
            .blendEnable(true)
            .alphaBlendOp(VK13.VK_BLEND_OP_ADD)
            .srcAlphaBlendFactor(VK13.VK_BLEND_FACTOR_ONE)
            .dstAlphaBlendFactor(VK13.VK_BLEND_FACTOR_ONE)
            .colorBlendOp(VK13.VK_BLEND_OP_ADD)
            .srcColorBlendFactor(VK13.VK_BLEND_FACTOR_ONE)
            .dstColorBlendFactor(VK13.VK_BLEND_FACTOR_ONE)

        UIpipeline = UIpipelineCreator
            .create(device, renderPass, UIVertex.properties,
            UIdescriptorSetLayout,
            VertexShader(device, "build/resources/main/shaders/ui.vert.spv"),
            FragmentShader(device, "build/resources/main/shaders/ui.frag.spv")
            )

        UIvertexBuffer = VertexBuffer(device, physicalDevice, 4, UIVertex.properties)
        UIindexBuffer = IndexBuffer(device, physicalDevice, commands, localPlayerView.vkUI.indexes)
        UIupdatingUniformBuffer = UpdatingUniformBuffer(device, physicalDevice, Config.MAX_FRAMES_IN_FLIGHT, Float.SIZE_BYTES * 2L)
        UIdescriptorSets = DescriptorSets(device, UIdescriptorPool, UIdescriptorSetLayout, UIupdatingUniformBuffer)

        for (frame in 0 until Config.MAX_FRAMES_IN_FLIGHT) {
            miniMapUpdatingUniformBuffer.update(
                frame,
                -1f,
                -1f,
                2f / Config.tileSize / localPlayerView.gameMap.fullTileSize,
            )
        }

        MemoryStack.stackPush().use { stack ->
            inFlightFences = Fences(Config.MAX_FRAMES_IN_FLIGHT, device, stack)

            imageAvailableSemaphore = Semaphores(Config.MAX_FRAMES_IN_FLIGHT, device, stack)

            renderFinishedSemaphore = Semaphores(Config.MAX_FRAMES_IN_FLIGHT, device, stack)
        }

    }

    override fun paintVK() {

        measureNanoTime {
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

                updatingUniformBuffer.update(
                    currentFrame,
                    localPlayerView.camera.offsetX,
                    localPlayerView.camera.offsetY,
                    localPlayerView.camera.scale,
                    width.toFloat(),
                    height.toFloat(),
                    ticksSinceStart.toFloat(),
                )
                ticksSinceStart += 1

                UIupdatingUniformBuffer.update(
                    currentFrame,
                    this.width.toFloat(),
                    this.height.toFloat()
                )

//                vertexBuffer.update(vertices)
                localPlayerView.gameObjectsView.update()
                objVertexBuffer.update(localPlayerView.gameObjectsView.vertexes)
                healthBarsVertexBuffer.update(localPlayerView.gameObjectsView.vertexesHealthBar)
                UIvertexBuffer.update(localPlayerView.vkUI.vertixes)

                record(currentFrame, imageIndex)

                val lpf = stack.mallocLong(1)
                val submitInfo = VkSubmitInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pNext(MemoryUtil.NULL)
                    .waitSemaphoreCount(1)
                    .pWaitSemaphores(Util.lp.put(0, imageAvailableSemaphore[currentFrame]))
                    .pWaitDstStageMask(Util.ip.put(0, VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pCommandBuffers(Util.pp.put(0, commands.commandBuffer[currentFrame]!!))
                    .pSignalSemaphores(lpf.put(0, renderFinishedSemaphore[currentFrame]))

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
                    .pWaitSemaphores(lpf)
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
//        println("fps: ${1e9 / time}")
    }

    fun record(currentFrame: Int, imageIndex: Int) {
        MemoryStack.stackPush().use { stack ->
            val beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)

            val clearColor = VkClearValue.calloc(1, stack)
                clearColor
                .color()
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

            val viewport = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(width.toFloat())
                .height(height.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            val scissor = VkRect2D.calloc(1, stack)
                .offset {
                    it.x(0)
                        .y(0)
                }
                .extent {
                    it.width(width)
                        .height(height)
                }

            val currentCommandBuffer = commands.commandBuffer[currentFrame]!!
            if (VK13.vkBeginCommandBuffer(currentCommandBuffer, beginInfo) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to begin recording command buffer!")
            }
            VK13.vkCmdBeginRenderPass(currentCommandBuffer, renderPassInfo, VK13.VK_SUBPASS_CONTENTS_INLINE)
            // map
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.graphicsPipeLine)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewport)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissor)

            val vertexBufferptr = stack.longs(vertexBuffer.buffer.vertexBuffer)
            val offsets = stack.longs(0)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, vertexBufferptr, offsets)

            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)

            val mapDescriptorSet = stack.longs(descriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.layout, 0, mapDescriptorSet, null)

            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.mapIndexes.size, 1, 0, 0, 0)

            //objects
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, objPipeline.graphicsPipeLine)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewport)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissor)

            val objVertexBufferptr = stack.longs(objVertexBuffer.buffer.vertexBuffer)
            val objOffsets = stack.longs(0)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, objVertexBufferptr, objOffsets)

            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)

            val objDescriptorSet = stack.longs(objDescriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, objPipeline.layout, 0, objDescriptorSet, null)

            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.getMapObjectsIndexCount(), 1, 0, 0, 0)

            // HealthBars
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, healthBarsPipeline.graphicsPipeLine)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewport)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissor)

            val healthBarsVertexBufferptr = stack.longs(healthBarsVertexBuffer.buffer.vertexBuffer)
            val healthBarsOffsets = stack.longs(0)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, healthBarsVertexBufferptr, healthBarsOffsets)

            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)

            val healthBarsDescriptorSet = stack.longs(healthBarsDescriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, healthBarsPipeline.layout, 0, healthBarsDescriptorSet, null)

            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.getMapObjectsIndexCount(), 1, 0, 0, 0)

            // ui
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, UIpipeline.graphicsPipeLine)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewport)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissor)

            val UIvertexBufferptr = stack.longs(UIvertexBuffer.buffer.vertexBuffer)
            val UIoffsets = stack.longs(0)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, UIvertexBufferptr, UIoffsets)
            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, UIindexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)

            val uiDescriptorSet = stack.longs(UIdescriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, UIpipeline.layout, 0, uiDescriptorSet, null)

            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.vkUI.getIndexesCount(), 1, 0, 0, 0)

            val viewportMiniMap = VkViewport.calloc(1, stack)
                .x(0.0f)
                .y(0.0f)
                .width(miniMapWidth.toFloat())
                .height(miniMapHeight.toFloat())
                .minDepth(0.0f)
                .maxDepth(1.0f)

            val scissorMiniMap = VkRect2D.calloc(1, stack)
                .offset {
                    it.x(0)
                        .y(0)
                }
                .extent {
                    it.width(miniMapWidth)
                        .height(miniMapHeight)
                }

            // minimap

            VK13.vkCmdNextSubpass(currentCommandBuffer, VK13.VK_SUBPASS_CONTENTS_INLINE)
            // map
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, miniMapPipeline.graphicsPipeLine)

            val miniMapDescriptorSet = stack.longs(miniMapDescriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewportMiniMap)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissorMiniMap)
            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, vertexBufferptr, offsets)
            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, miniMapPipeline.layout, 0, miniMapDescriptorSet, null)
            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.mapIndexes.size, 1, 0, 0, 0)

            //objects
            VK13.vkCmdBindPipeline(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, objMiniMapPipeline.graphicsPipeLine)

            VK13.vkCmdSetViewport(currentCommandBuffer, 0, viewportMiniMap)
            VK13.vkCmdSetScissor(currentCommandBuffer, 0, scissorMiniMap)

            VK13.vkCmdBindVertexBuffers(currentCommandBuffer, 0, objVertexBufferptr, objOffsets)
            VK13.vkCmdBindIndexBuffer(currentCommandBuffer, indexBuffer.buffer.vertexBuffer, 0, VK13.VK_INDEX_TYPE_UINT32)
            val miniMapObjDescriptorSet = stack.longs(miniMapObjDescriptorSets.descriptorSets[currentFrame])
            VK13.vkCmdBindDescriptorSets(currentCommandBuffer, VK13.VK_PIPELINE_BIND_POINT_GRAPHICS, objMiniMapPipeline.layout, 0, miniMapObjDescriptorSet, null)
            VK13.vkCmdDrawIndexed(currentCommandBuffer, localPlayerView.getMapObjectsIndexCount(), 1, 0, 0, 0)

            VK13.vkCmdEndRenderPass(currentCommandBuffer)

            if (VK13.vkEndCommandBuffer(currentCommandBuffer) != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to record command buffer")
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

    override fun update(g: Graphics?) {
        paint(g)
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