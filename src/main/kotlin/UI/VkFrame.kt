package UI

import Frame.VkGame
import GameMap.GameObjects.GameObject
import View.LocalPlayerView
import VkRender.Instance
import javax.swing.JFrame
import VkRender.VkCanvas
import java.awt.event.ActionListener
import javax.swing.Timer

class VkFrame(
    localPlayerView: LocalPlayerView,
    FPS: Int = 60,
    loopFunction: () -> Unit,
    skillButtonsListener: ActionListener,
    ) {

    val frame: JFrame
    private val vkInstance: Instance = Instance("RTS")
    val canvas: VkCanvas

    val loopTimer: Timer

    val content: VkGame

    init {

        frame = JFrame("RTS")

//        frame.layout = BorderLayout()

//        frame.preferredSize = Dimension(800, 800)

        canvas = VkCanvas(vkInstance, localPlayerView)
        content = VkGame(canvas)
        content.createUIComponents()

        content.skillTable.forEach { it.addActionListener(skillButtonsListener) }

        frame.contentPane = content.mainPanel

//        frame.add(canvas, BorderLayout.CENTER)

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

    fun selectObject(obj: GameObject) {
        if (obj.abilities != null) {
            var i = 0
            for ((key, ability) in obj.abilities!!) {
                content.skillTable[i].isEnabled = true
                content.skillTable[i].text = key
                content.skillTable[i].toolTipText = key
                content.skillTable[i].name = key
                i++
            }
            for (j in i..15) {
                content.skillTable[j].isEnabled = false
            }
        } else {
            content.skillTable.forEach { it.isEnabled = false }
        }
    }

}