package UI

import Frame.VkGame
import GameMap.GameObjects.GameObject
import View.LocalPlayerView
import VkRender.Instance
import javax.swing.JFrame
import VkRender.VkCanvas
import java.awt.event.ActionListener
import java.lang.Integer.min
import javax.swing.DefaultListModel
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
        content.selectedUnitList.addListSelectionListener {
            try {
                val obj = content.selectedUnitList.model.getElementAt(content.selectedUnitList.selectedIndex) as GameObject
                selectObject(obj)
            } catch (e: ArrayIndexOutOfBoundsException) {
            }
        }

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

    fun updateSelectedObjects(objects: List<GameObject>) {
        val model = (content.selectedUnitList.model as DefaultListModel)
        model.clear()
        for (obj in objects) {
            model.addElement(obj)
        }
//        val obj = objects.first()
        content.selectedUnitList.clearSelection()
        content.selectedUnitList.selectedIndex = 0
//        selectObject(obj)
    }

    fun deselect() {
        for (j in 0..15) {
            content.skillTable[j].isEnabled = false
            content.skillTable[j].text = ""
            content.skillTable[j].toolTipText = ""
            content.skillTable[j].name = ""
        }
        content.unitName.text = ""
        content.hpBar.maximum = 0
        content.hpBar.value = 0
        content.hpBar.string = "0 / 0"
        (content.selectedUnitList.model as DefaultListModel).clear()
    }

    fun selectObject(obj: GameObject?) {
        if (obj != null) {
            if (obj.abilities != null) {
                var i = 0
                for ((key, _) in obj.abilities!!) {
                    content.skillTable[i].isEnabled = true
                    content.skillTable[i].text = key
                    content.skillTable[i].toolTipText = key
                    content.skillTable[i].name = key
                    i++
                }
                for (j in i..15) {
                    content.skillTable[j].isEnabled = false
                    content.skillTable[j].text = ""
                    content.skillTable[j].toolTipText = ""
                    content.skillTable[j].name = ""
                }
            }
            content.unitName.text = obj.javaClass.simpleName
            content.hpBar.maximum = obj.maxHealth.toInt()
            content.hpBar.value = obj.health.toInt()
            content.hpBar.string = obj.health.toString() + " / " + obj.maxHealth.toString();
        } else {
            deselect()
        }
    }
}