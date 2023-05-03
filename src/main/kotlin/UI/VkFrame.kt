package UI

import View.LocalPlayerView
import VkRender.Instance
import javax.swing.JFrame
import VkRender.VkCanvas
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Timer

class VkFrame(title: String, localPlayerView: LocalPlayerView, FPS: Int = 60, loopFunction: () -> Unit) {

    public val frame: JFrame
    private val vkInstance: Instance
    val canvas: VkCanvas

    val loopTimer: Timer;

    init {

        vkInstance = Instance("RTS")

        frame = JFrame(title)
        frame.layout = BorderLayout()

        frame.preferredSize = Dimension(800, 800)

        canvas = VkCanvas(vkInstance, localPlayerView)
        frame.add(canvas, BorderLayout.CENTER)

        loopTimer = Timer(1000 / FPS) {
            loopFunction()
        }
    }

    fun start() {
        loopTimer.start()
        frame.pack()
        frame.isVisible = true
    }

    fun repaintCanvas() {
        canvas.repaint()
    }

}