
import VkRender.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Label
import javax.swing.JFrame

fun main(args: Array<String>) {
    val instance = Instance("Vulkan awt")

    val frame = JFrame("AWT VK test")
    frame.layout = BorderLayout()
    frame.preferredSize = Dimension(800, 600)

    val canvas = VkCanvas(instance)
    frame.add(canvas, BorderLayout.CENTER)
    val label = Label("I'm label!")
    label.setBounds(400, 200, 100, 20)
    frame.layeredPane.add(label)
    frame.pack()
    frame.isVisible = true

//        renderPass.close()
//        swapChain.close()
//        device.close()
//        physicalDevice.close()
//        surface.close()
//        instance.close()
}
