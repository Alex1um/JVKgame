package VkRender.Surfaces

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI

class Window(var width: Int, var height: Int, title: String, callback: GLFWFramebufferSizeCallbackI? = null) {

    val window: Long
    init {

        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Cannot init glfw!")
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        window = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        GLFW.glfwSetFramebufferSizeCallback(window, callback)



    }
}