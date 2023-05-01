package VkRender.GPUObjects

import org.joml.*
import org.lwjgl.vulkan.VK13

open class GPUObjectAutoProperties(vararg structure: Any) {

    val properties: Properties
//    val SIZEOF: Int,
//    val OFFSETS: Array<Int>,
//    val FORMATS: Array<Int>,

    init {
        var sizeOf: Int = 0
        val offsets: MutableList<Int> = mutableListOf()
        val formats: MutableList<Int> = mutableListOf()

        for (obj in structure) {
            when (obj) {
                is Int, is Int.Companion -> {
                    offsets.add(sizeOf)
                    sizeOf += Int.SIZE_BYTES
                    formats.add(VK13.VK_FORMAT_R32_SINT)
                }
                is Vector2ic -> {
                    offsets.add(sizeOf)
                    sizeOf += Int.SIZE_BYTES * 2
                    formats.add(VK13.VK_FORMAT_R32G32_SINT)
                }
                is Vector3ic -> {
                    offsets.add(sizeOf)
                    sizeOf += Int.SIZE_BYTES * 3
                    formats.add(VK13.VK_FORMAT_R32G32B32_SINT)
                }
                is Vector4ic -> {
                    offsets.add(sizeOf)
                    sizeOf += Int.SIZE_BYTES * 4
                    formats.add(VK13.VK_FORMAT_R32G32B32A32_SINT)
                }
                is Float, is Float.Companion -> {
                    offsets.add(sizeOf)
                    sizeOf += Float.SIZE_BYTES
                    formats.add(VK13.VK_FORMAT_R32_SFLOAT)
                }
                is Vector2fc -> {
                    offsets.add(sizeOf)
                    sizeOf += Float.SIZE_BYTES * 2
                    formats.add(VK13.VK_FORMAT_R32G32_SFLOAT)
                }
                is Vector3fc -> {
                    offsets.add(sizeOf)
                    sizeOf += Float.SIZE_BYTES * 3
                    formats.add(VK13.VK_FORMAT_R32G32B32_SFLOAT)
                }
                is Vector4fc -> {
                    offsets.add(sizeOf)
                    sizeOf += Float.SIZE_BYTES * 4
                    formats.add(VK13.VK_FORMAT_R32G32B32A32_SFLOAT)
                }
                else -> throw IllegalStateException("Type is not supported")
            }
        }
        properties = Properties(sizeOf, offsets.toTypedArray(), formats.toTypedArray())
//        SIZEOF = sizeOf
//        OFFSETS = offsets.toTypedArray()
//        FORMATS = formats.toTypedArray()
    }
}