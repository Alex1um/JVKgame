package VkRender.Pipelines

import VkRender.Descriptors.FilledDescriptorSetLayout
import VkRender.Device
import VkRender.GPUObjects.Properties
import VkRender.RenderPass
import VkRender.ShaderModule.ShaderModule
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.*
import kotlin.Lazy
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class GraphicsPipelineCreator(
) {

    private val stack: MemoryStack = MemoryStack.stackPush();

    val inputAssembly: VkPipelineInputAssemblyStateCreateInfo by lazy {
        VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
    }
    val viewportState: VkPipelineViewportStateCreateInfo by lazy {
        VkPipelineViewportStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
    }
    val rasterizer: VkPipelineRasterizationStateCreateInfo by lazy {
        VkPipelineRasterizationStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
    }
    val multisampling: VkPipelineMultisampleStateCreateInfo by lazy {
        VkPipelineMultisampleStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
    }
    val colorBlendAttachment: VkPipelineColorBlendAttachmentState.Buffer by lazy {
        VkPipelineColorBlendAttachmentState.calloc(1, stack)
    }
    val colorBlending: VkPipelineColorBlendStateCreateInfo by lazy {
        VkPipelineColorBlendStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
    }
    val dynamicState: VkPipelineDynamicStateCreateInfo by lazy {
        VkPipelineDynamicStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
    }

    val KProperty0<*>.isLazyInitialized: Boolean
        get() {
            if (this !is Lazy<*>) return true
            return (this.getDelegate() as Lazy<*>).isInitialized()
        }

    fun fillDefault(): GraphicsPipelineCreator {
        inputAssembly
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
            .topology(VK13.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
            .primitiveRestartEnable(false)


        viewportState
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
            .viewportCount(1)
            .scissorCount(1)

        rasterizer
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

        multisampling
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
            .sampleShadingEnable(false)
            .rasterizationSamples(VK13.VK_SAMPLE_COUNT_1_BIT)
            .minSampleShading(1.0f)
            .pSampleMask(null)
            .alphaToCoverageEnable(false)
            .alphaToOneEnable(false)

        colorBlendAttachment
            .colorWriteMask(VK13.VK_COLOR_COMPONENT_A_BIT or VK13.VK_COLOR_COMPONENT_B_BIT or VK13.VK_COLOR_COMPONENT_G_BIT or VK13.VK_COLOR_COMPONENT_R_BIT)
            .blendEnable(false)

        colorBlending
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
            .logicOpEnable(false)
            .logicOp(VK13.VK_LOGIC_OP_COPY)
            .pAttachments(colorBlendAttachment)
            .blendConstants(0, 0.3f)
            .blendConstants(1, 0.5f)
            .blendConstants(2, 0.7f)
            .blendConstants(3, 1.0f)

        return this
    }

    fun makeViewPortAndScissorDynamicStates() : GraphicsPipelineCreator {
        val dynamicStates = stack.ints(VK13.VK_DYNAMIC_STATE_VIEWPORT, VK13.VK_DYNAMIC_STATE_SCISSOR)
        dynamicState
            .pDynamicStates(dynamicStates)
        return this
    }
    fun create(
        ldevice: Device,
        basePipeline: GraphicsPipeline,
        vertexProperties: Properties,
        renderPass: RenderPass? = null,
        descriptorSetLayout: FilledDescriptorSetLayout? = null,
        vararg shaderModules: ShaderModule,
        subpass: Int = 0,
    ) : GraphicsPipeline {

        val shaderStagesInfo = VkPipelineShaderStageCreateInfo.calloc(shaderModules.size, stack)

        val main = stack.UTF8("main")
        for ((i, module) in shaderModules.withIndex()) {
            shaderStagesInfo[i]
                .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(module.shaderType)
                .module(module.module)
                .pName(main)
        }

        val bindingDescription = vertexProperties.getBindingDescription(stack, 0)
        val attributeDescription = vertexProperties.getAttributeDescriptions(stack, 0)

        val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc()
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
            .pVertexBindingDescriptions(bindingDescription)
            .pVertexAttributeDescriptions(attributeDescription)

        val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
            .sType(VK13.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
            .pStages(shaderStagesInfo)
            .pVertexInputState(vertexInputInfo)

        if (this::inputAssembly.isLazyInitialized) {
            pipelineInfo
                .pInputAssemblyState(inputAssembly)
            }
        if (this::viewportState.isLazyInitialized) {
            pipelineInfo
                .pViewportState(viewportState)
        }
        if (this::rasterizer.isLazyInitialized) {
            pipelineInfo
                .pRasterizationState(rasterizer)
        }
        if (this::multisampling.isLazyInitialized) {
            pipelineInfo
                .pMultisampleState(multisampling)
        }
        if (this::colorBlending.isLazyInitialized) {
            pipelineInfo
                .pColorBlendState(colorBlending)
        }
        if (this::dynamicState.isLazyInitialized) {
            pipelineInfo
                .pDynamicState(dynamicState)
        }
        if (renderPass != null) {
            pipelineInfo
                .renderPass(renderPass.renderPass)
        }
        pipelineInfo
            .pDepthStencilState(null)
            .subpass(subpass)
            .basePipelineHandle(basePipeline.graphicsPipeLine)
            .basePipelineIndex(-1)

        val layouts = if (descriptorSetLayout != null) {
            stack.longs(descriptorSetLayout.descriptorSetLayout)
        } else {
            null
        }

        val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
            .pSetLayouts(layouts)
            .pPushConstantRanges(null)

        if (VK13.vkCreatePipelineLayout(ldevice.device, pipelineLayoutInfo, null, Util.lp) != VK13.VK_SUCCESS) {
            throw IllegalStateException("failed to create pipeline layout!")
        }
        val layout = Util.lp[0]
        pipelineInfo
            .layout(layout)

        if (VK13.vkCreateGraphicsPipelines(
                ldevice.device,
                VK13.VK_NULL_HANDLE,
                pipelineInfo,
                null,
                Util.lp
            ) != VK13.VK_SUCCESS
        ) {
            throw IllegalStateException("failed to create graphics pipeline")
        }
        val graphicsPipeLine = Util.lp[0]

        for (module in shaderModules) {
            module.close()
        }
        stack.close()

        return GraphicsPipeline(ldevice, layout, graphicsPipeLine)
    }

    fun create(
        ldevice: Device,
        renderPass: RenderPass,
        vertexProperties: Properties,
        descriptorSetLayout: FilledDescriptorSetLayout? = null,
        vararg shaderModules: ShaderModule,
        subpass: Int = 0,
    ) : GraphicsPipeline {

        val shaderStagesInfo = VkPipelineShaderStageCreateInfo.calloc(shaderModules.size, stack)

        val main = stack.UTF8("main")
        for ((i, module) in shaderModules.withIndex()) {
            shaderStagesInfo[i]
                .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(module.shaderType)
                .module(module.module)
                .pName(main)
        }

        val bindingDescription = vertexProperties.getBindingDescription(stack, 0)
        val attributeDescription = vertexProperties.getAttributeDescriptions(stack, 0)

        val vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc()
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
            .pVertexBindingDescriptions(bindingDescription)
            .pVertexAttributeDescriptions(attributeDescription)

        val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
            .topology(VK13.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
            .primitiveRestartEnable(false)

        val pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
            .sType(VK13.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
            .pStages(shaderStagesInfo)
            .pVertexInputState(vertexInputInfo)
            .pInputAssemblyState(inputAssembly)
            .pViewportState(viewportState)
            .pRasterizationState(rasterizer)
            .pMultisampleState(multisampling)
            .pDepthStencilState(null)
            .pColorBlendState(colorBlending)
            .pDynamicState(dynamicState)
            .renderPass(renderPass.renderPass)
            .subpass(subpass)
            .basePipelineHandle(VK13.VK_NULL_HANDLE)
            .basePipelineIndex(-1)

        val layouts = if (descriptorSetLayout != null) {
            stack.longs(descriptorSetLayout.descriptorSetLayout)
        } else {
            null
        }

        val pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
            .sType(VK13.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
            .pSetLayouts(layouts)
            .pPushConstantRanges(null)

        if (VK13.vkCreatePipelineLayout(ldevice.device, pipelineLayoutInfo, null, Util.lp) != VK13.VK_SUCCESS) {
            throw IllegalStateException("failed to create pipeline layout!")
        }
        val layout = Util.lp[0]
        pipelineInfo
            .layout(layout)

        if (VK13.vkCreateGraphicsPipelines(
                ldevice.device,
                VK13.VK_NULL_HANDLE,
                pipelineInfo,
                null,
                Util.lp
            ) != VK13.VK_SUCCESS
        ) {
            throw IllegalStateException("failed to create graphics pipeline")
        }
        val graphicsPipeLine = Util.lp[0]

        for (module in shaderModules) {
            module.close()
        }
        stack.close()

        return GraphicsPipeline(ldevice, layout, graphicsPipeLine)

    }

}