package VkRender

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class GraphicsPipeline(
    private val ldevice: Device,
    renderPass: RenderPass,
    width: Int,
    height: Int
) : Closeable {

    val graphicsPipeLine: Long
    val layout: Long

    init {
        with(Util) {
            val vertShaderModule = ShaderModule(ldevice, "build/resources/main/shaders/vert.spv")
            val fragShaderModule = ShaderModule(ldevice, "build/resources/main/shaders/frag.spv")

            MemoryStack.stackPush().use { stack ->
                val shaderStagesInfo = VkPipelineShaderStageCreateInfo.calloc(2, stack)

                val main = stack.UTF8("main")

                shaderStagesInfo[0]
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK13.VK_SHADER_STAGE_VERTEX_BIT)
                    .module(vertShaderModule.module)
                    .pName(main)
                shaderStagesInfo[1]
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK13.VK_SHADER_STAGE_FRAGMENT_BIT)
                    .module(fragShaderModule.module)
                    .pName(main)

                val dynamicStates = stack.ints(VK13.VK_DYNAMIC_STATE_VIEWPORT, VK13.VK_DYNAMIC_STATE_SCISSOR)

                val dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(dynamicStates)

                val bindingDescription = Vertex.getBindingDescription(stack)
                val attributeDescription = Vertex.getAttributeDescriptions(stack)

                val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc()
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                    .pVertexBindingDescriptions(bindingDescription)
                    .pVertexAttributeDescriptions(attributeDescription)

                val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK13.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                    .primitiveRestartEnable(false)

//                val viewPort = VkViewport.calloc(1, stack)
//                    .x(0.0f)
//                    .y(0.0f)
//                    .width(width.toFloat())
//                    .height(height.toFloat())
//                    .minDepth(0.0f)
//                    .maxDepth(1.0f)

//                val scissor = VkRect2D.calloc(1, stack)
//                    .offset {
//                        it.x(0)
//                            .y(0)
//                    }
////                .extent(swapChainExtent)
//                    .extent {
//                        it.width(width)
//                            .height(height)
//                    }

                val viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .viewportCount(1)
                    .scissorCount(1)
//                    .pScissors(scissor)
//                    .pViewports(viewPort)

                val rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK13.VK_POLYGON_MODE_FILL)
                    .lineWidth(1.0f)
                    .cullMode(VK13.VK_CULL_MODE_BACK_BIT)
                    .frontFace(VK13.VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false)
//                    .depthBiasConstantFactor(0.0f)
//                    .depthBiasClamp(0.0f)
//                    .depthBiasSlopeFactor(0.0f)

                val multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
//                    .rasterizationSamples(VK13.VK_SAMPLE_COUNT_1_BIT)
//                    .minSampleShading(1.0f)
//                    .pSampleMask(null)
//                    .alphaToCoverageEnable(false)
//                    .alphaToOneEnable(false)

                val colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
                    .colorWriteMask(VK13.VK_COLOR_COMPONENT_A_BIT or VK13.VK_COLOR_COMPONENT_B_BIT or VK13.VK_COLOR_COMPONENT_G_BIT or VK13.VK_COLOR_COMPONENT_R_BIT)
//                .blendEnable(true)
//                .srcColorBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstColorBlendFactor(VK_BLEND_FACTOR_ONE)
//                .colorBlendOp(VK_BLEND_OP_SUBTRACT)
//                .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
//                .alphaBlendOp(VK_BLEND_OP_ADD)

                    .blendEnable(false)
//                    .srcColorBlendFactor(VK13.VK_BLEND_FACTOR_SRC_ALPHA)
//                    .dstColorBlendFactor(VK13.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
//                    .colorBlendOp(VK13.VK_BLEND_OP_ADD)
//                    .srcAlphaBlendFactor(VK13.VK_BLEND_FACTOR_ONE)
//                    .dstAlphaBlendFactor(VK13.VK_BLEND_FACTOR_ZERO)
//                    .alphaBlendOp(VK13.VK_BLEND_OP_ADD)

                val colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(false)
                    .logicOp(VK13.VK_LOGIC_OP_COPY)
                    .pAttachments(colorBlendAttachment)
                    .blendConstants(0, 0.3f)
                    .blendConstants(1, 0.5f)
                    .blendConstants(2, 0.7f)
                    .blendConstants(3, 1.0f)

                val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pSetLayouts(null)
                    .pPushConstantRanges(null)

                if (VK13.vkCreatePipelineLayout(ldevice.device, pipelineLayoutInfo, null, lp) != VK13.VK_SUCCESS) {
                    throw IllegalStateException("failed to create pipeline layout!")
                }
                layout = lp[0]

                val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(shaderStagesInfo)
                    .pVertexInputState(vertexInputInfo)
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizer)
//                    .pMultisampleState(multisampling)
                    .pDepthStencilState(null)
                    .pColorBlendState(colorBlending)
                    .pDynamicState(dynamicState)
                    .layout(layout)
                    .renderPass(renderPass.renderPass)
                    .subpass(0)
                    .basePipelineHandle(VK13.VK_NULL_HANDLE)
                    .basePipelineIndex(-1)

                if (VK13.vkCreateGraphicsPipelines(
                        ldevice.device,
                        VK13.VK_NULL_HANDLE,
                        pipelineInfo,
                        null,
                        lp
                    ) != VK13.VK_SUCCESS
                ) {
                    throw IllegalStateException("failed to create graphics pipeline")
                }
                graphicsPipeLine = lp[0]
            }
            fragShaderModule.close()
            vertShaderModule.close()
        }
    }

    inner class ShaderModule(private val ldevice: Device, data: ByteArray) : Closeable {
        val module: Long


        constructor(ldevice: Device, path: String) : this(ldevice, Util.readFile(path))

        init {
            MemoryStack.stackPush().use { stack ->
                val pCode = MemoryUtil.memAlloc(data.size).put(data)
                pCode.flip()
                val createInfo = VkShaderModuleCreateInfo.malloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(pCode)
                    .flags(0)
                    .pNext(MemoryUtil.NULL)
                val rt = VK13.vkCreateShaderModule(ldevice.device, createInfo, null, Util.lp)
                if (rt != VK13.VK_SUCCESS) {
                    throw IllegalStateException("failed to create shader module!")
                }
                MemoryUtil.memFree(pCode)
                module = Util.lp[0]
            }
        }

        override fun close() {
            VK13.vkDestroyShaderModule(ldevice.device, module, null)
        }

    }

    override fun close() {
        VK13.vkDestroyPipeline(ldevice.device, graphicsPipeLine, null)
        VK13.vkDestroyPipelineLayout(ldevice.device, layout, null)

    }

}