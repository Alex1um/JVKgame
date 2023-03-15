
import VkRender.*
import java.awt.*
import javax.swing.JFrame
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

fun main(args: Array<String>) {
    val instance = Instance("Vulkan awt")

    val frame = JFrame("AWT VK test")
    frame.layout = BorderLayout()
    frame.preferredSize = Dimension(800, 600)

    val canvas = VkCanvas(instance)
//    val canvas = Canvas()
    frame.add(canvas, BorderLayout.CENTER)
    val label = Label("I'm label!")
    label.setBounds(400, 200, 100, 20)
    frame.layeredPane.add(label)
    frame.pack()
    frame.isVisible = true
//    frame.paint(frame.graphics)
//    Thread.sleep(1000)
//    while (true) {
//        val time = 1e9 / measureNanoTime {
//            canvas.paint(canvas.graphics)
//            frame.paint(frame.graphics)
//        }
//        println(
//            "secons per frame: ${
//                time
//            }"
//        )
//    }
//        renderPass.close()
//        swapChain.close()
//        device.close()
//        physicalDevice.close()
//        surface.close()
//        instance.close()
}
