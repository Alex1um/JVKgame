package VkRender

import VkRender.Descriptors.DescriptorSetLayout
import VkRender.GPUObjects.Properties
import VkRender.ShaderModule.ShaderModule
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK13.*
import java.io.Closeable

class GraphicsPipeline(
    private val ldevice: Device,
    renderPass: RenderPass,
    descriptorSetLayout: DescriptorSetLayout,
    vertexProperties: Properties,
    vararg shaderModules: ShaderModule
) : Closeable {

    val graphicsPipeLine: Long
    val layout: Long

    init {
        with(Util) {
//            val vertShaderModule = ShaderModule(ldevice, "build/resources/main/shaders/vert.spv")
//            val fragShaderModule = ShaderModule(ldevice, "build/resources/main/shaders/frag.spv")

            MemoryStack.stackPush().use { stack ->
                val shaderStagesInfo = VkPipelineShaderStageCreateInfo.calloc(shaderModules.size, stack)

                val main = stack.UTF8("main")
                for ((i, module) in shaderModules.withIndex()) {
                    shaderStagesInfo[i]
                        .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                        .stage(module.shaderType)
                        .module(module.module)
                        .pName(main)
                }
//                shaderStagesInfo[0]
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
//                    .stage(VK_SHADER_STAGE_VERTEX_BIT)
//                    .module(vertShaderModule.module)
//                    .pName(main)
//                shaderStagesInfo[1]
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
//                    .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
//                    .module(fragShaderModule.module)
//                    .pName(main)

                val dynamicStates = stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR)

                val dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(dynamicStates)

                val bindingDescription = vertexProperties.getBindingDescription(stack, 0)
                val attributeDescription = vertexProperties.getAttributeDescriptions(stack, 0)

                val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                    .pVertexBindingDescriptions(bindingDescription)
                    .pVertexAttributeDescriptions(attributeDescription)

                val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
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
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .viewportCount(1)
                    .scissorCount(1)
//                    .pScissors(scissor)
//                    .pViewports(viewPort)

                val rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK_POLYGON_MODE_FILL)
                    .lineWidth(1.0f)
                    .cullMode(VK_CULL_MODE_BACK_BIT)
                    .frontFace(VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false)
//                    .depthBiasConstantFactor(0.0f)
//                    .depthBiasClamp(0.0f)
//                    .depthBiasSlopeFactor(0.0f)

                val multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
                    .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
                    .minSampleShading(1.0f)
                    .pSampleMask(null)
                    .alphaToCoverageEnable(false)
                    .alphaToOneEnable(false)

                val colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
                    .colorWriteMask(VK_COLOR_COMPONENT_A_BIT or VK_COLOR_COMPONENT_B_BIT or VK_COLOR_COMPONENT_G_BIT or VK_COLOR_COMPONENT_R_BIT)
//                .blendEnable(true)
//                .srcColorBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstColorBlendFactor(VK_BLEND_FACTOR_ONE)
//                .colorBlendOp(VK_BLEND_OP_SUBTRACT)
//                .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
//                .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
//                .alphaBlendOp(VK_BLEND_OP_ADD)
//
                    .blendEnable(false)
//                    .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
//                    .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
//                    .colorBlendOp(VK_BLEND_OP_ADD)
//                    .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
//                    .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
//                    .alphaBlendOp(VK_BLEND_OP_ADD)

                val colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(false)
                    .logicOp(VK_LOGIC_OP_COPY)
                    .pAttachments(colorBlendAttachment)
                    .blendConstants(0, 0.3f)
                    .blendConstants(1, 0.5f)
                    .blendConstants(2, 0.7f)
                    .blendConstants(3, 1.0f)

                val layouts = stack.longs(descriptorSetLayout.descriptorSetLayout)

                val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pSetLayouts(layouts)
                    .pPushConstantRanges(null)

                if (vkCreatePipelineLayout(ldevice.device, pipelineLayoutInfo, null, lp) != VK_SUCCESS) {
                    throw IllegalStateException("failed to create pipeline layout!")
                }
                layout = lp[0]

                val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(shaderStagesInfo)
                    .pVertexInputState(vertexInputInfo)
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizer)
                    .pMultisampleState(multisampling)
                    .pDepthStencilState(null)
                    .pColorBlendState(colorBlending)
                    .pDynamicState(dynamicState)
                    .layout(layout)
                    .renderPass(renderPass.renderPass)
                    .subpass(0)
                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1)

                if (vkCreateGraphicsPipelines(
                        ldevice.device,
                        VK_NULL_HANDLE,
                        pipelineInfo,
                        null,
                        lp
                    ) != VK_SUCCESS
                ) {
                    throw IllegalStateException("failed to create graphics pipeline")
                }
                graphicsPipeLine = lp[0]
            }

            for (module in shaderModules) {
                module.close()
            }
//            fragShaderModule.close()
//            vertShaderModule.close()
        }
    }

    override fun close() {
        vkDestroyPipeline(ldevice.device, graphicsPipeLine, null)
        vkDestroyPipelineLayout(ldevice.device, layout, null)
    }

}