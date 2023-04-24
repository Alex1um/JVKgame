package UI

import View.LocalPlayerView
import VkRender.Instance
import javax.swing.JFrame
import VkRender.VkCanvas
import java.awt.BorderLayout
import java.awt.Dimension

class VkFrame(title: String, localPlayerView: LocalPlayerView) {

    private val frame: JFrame
    private val vkInstance: Instance
    val canvas: VkCanvas

    init {

        vkInstance = Instance("RTS")

        frame = JFrame(title)
        frame.layout = BorderLayout()

        frame.preferredSize = Dimension(800, 800)

        canvas = VkCanvas(vkInstance, localPlayerView)
        frame.add(canvas, BorderLayout.CENTER)


    }

    fun start() {
        frame.pack()
        frame.isVisible = true
    }

}