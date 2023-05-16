package VkRender.Surfaces

import VkRender.Instance
import org.lwjgl.vulkan.KHRSurface

class NativeSurface//    constructor(window: Window, instance: Instance) {
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
    (override val surface: Long, override val instance: Instance) : Surface {

    override fun close() {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, surface, null)
    }

}