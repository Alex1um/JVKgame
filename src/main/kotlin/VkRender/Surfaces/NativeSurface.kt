package VkRender.Surfaces

import VkRender.Instance
import VkRender.Util
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.VK13

class NativeSurface: Surface {

    override val surface: Long
    override val instance: Instance

//    constructor(window: Window, instance: Instance) {
//        this.instance = instance
//        with (Util) {
//            MemoryStack.stackPush().use { stack ->
////            val lp = stack.longs(0)
//                if (GLFWVulkan.glfwCreateWindowSurface(instance.instance, window.window, null, lp) != VK13.VK_SUCCESS) {
//                    throw IllegalStateException("failed to create window surface")
//                }
//                surface = lp[0]
//            }
//        }
//    }
//
    constructor(surface: Long, instance: Instance) {
        this.instance = instance
        this.surface = surface
    }

    override fun close() {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, surface, null)
    }

}