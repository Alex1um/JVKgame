package VkRender.Surfaces

import VkRender.Instance
import org.lwjgl.vulkan.KHRSurface
import java.io.Closeable

interface Surface : Closeable {
    val surface: Long;
    val instance: Instance

    override fun close() {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, surface, null)
    }
}