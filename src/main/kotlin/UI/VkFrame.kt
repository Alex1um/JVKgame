package UI

import Controller.Controller
import GameMap.GameMap
import View.LocalPlayerView
import VkRender.Config
import VkRender.Vertex
import VkRender.Instance
import javax.swing.JFrame
import VkRender.VkCanvas
import VkRender.buffers.IndexBuffer
import java.awt.BorderLayout
import java.awt.Dimension

class VkFrame(title: String, controller: Controller) {

    private val frame: JFrame
    private val vkInstance: Instance
    private val canvas: VkCanvas

    init {

        vkInstance = Instance("RTS")

        frame = JFrame(title)
        frame.layout = BorderLayout()

        frame.preferredSize = Dimension(800, 800)

        canvas = VkCanvas(vkInstance, controller)
        frame.add(canvas, BorderLayout.CENTER)

        frame.pack()
        frame.isVisible = true

    }

}