package VkRender

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import org.lwjgl.vulkan.*
import java.io.Closeable
import java.nio.ByteBuffer

class Instance(app_name: String, VALIDATE: Boolean = Config.VALIDATE) : Closeable {

    val instance: VkInstance

    init {

        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Cannot init glfw!")
        }


        MemoryStack.stackPush().use { stack ->

            val APP_SHORT_NAME: ByteBuffer = stack.UTF8(app_name)
            val appInfo: VkApplicationInfo = VkApplicationInfo.malloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(APP_SHORT_NAME)
                .applicationVersion(VK13.VK_MAKE_VERSION(0, 0, 1))
                .pEngineName(APP_SHORT_NAME)
                .engineVersion(VK13.VK_MAKE_VERSION(0, 0, 1))
                .apiVersion(VK13.VK_API_VERSION_1_3)
                .pNext(MemoryUtil.NULL)

            val ptrBuf = stack.mallocPointer(64)

            if (VALIDATE) {
                ptrBuf.clear()
                ptrBuf.put(stack.ASCII("VK_LAYER_KHRONOS_validation"))
                ptrBuf.flip()
            }

//                val glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions()
//                    ?: throw IllegalStateException("glfwGetRequiredInstanceExtensions failed to find the platform surface extensions.")

            val platformSurfaceExtension = when (Platform.get()) {
                Platform.WINDOWS -> KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME
                Platform.LINUX -> if (System.getenv("XDG_SESSION_TYPE") == "x11") KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME else KHRWaylandSurface.VK_KHR_WAYLAND_SURFACE_EXTENSION_NAME
//                    Platform.MACOSX -> throw IllegalStateException("macOS are not supported")
                else -> throw IllegalStateException("Platform are not supported")
            }

            val glfwExtensions = stack.pointers(stack.UTF8(KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME), stack.UTF8(
                KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME))

            val createInfo = VkInstanceCreateInfo.malloc(stack)
                .sType(VK13.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo)
                .ppEnabledLayerNames(ptrBuf)
                .pNext(MemoryUtil.NULL)
                .ppEnabledExtensionNames(glfwExtensions)

//                createInfo = createInfo.ppEnabledExtensionNames(glfwExtensions)
            val tmp_ptr = stack.pointers(0)
            val result = VK13.vkCreateInstance(createInfo, null, tmp_ptr)
            if (result != VK13.VK_SUCCESS) {
                throw IllegalStateException("failed to create instance!")
            }
            instance = VkInstance(tmp_ptr[0], createInfo)
        }
    }

    override fun close() {
        VK13.vkDestroyInstance(instance, null)
    }

}