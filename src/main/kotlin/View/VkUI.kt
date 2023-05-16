package View

import VkRender.GPUObjects.UIVertex
import org.joml.Vector2f
import org.joml.Vector4f
import java.awt.Point
import kotlin.math.sign

class VkUI {

    private val selectionRect: List<UIVertex> = listOf(
        UIVertex(Vector2f(0f, 0f), Vector4f(0f, 1f, 0f, .5f)),
        UIVertex(Vector2f(0f, 0f), Vector4f(0f, 1f, 0f, .5f)),
        UIVertex(Vector2f(0f, 0f), Vector4f(0f, 1f, 0f, .5f)),
        UIVertex(Vector2f(0f, 0f), Vector4f(0f, 1f, 0f, .5f)),
    )

    val vertixes: List<UIVertex> = selectionRect

    val indexes = listOf(
        0, 1, 3, 3, 2, 0,
    )

    fun select(start: Point, end: Point) {
        if ((start.x - end.x).sign == (start.y - end.y).sign) {
            selectionRect[0].pos = Vector2f(start.x.toFloat(), start.y.toFloat())
            selectionRect[1].pos = Vector2f(end.x.toFloat(), start.y.toFloat())
            selectionRect[2].pos = Vector2f(start.x.toFloat(), end.y.toFloat())
            selectionRect[3].pos = Vector2f(end.x.toFloat(), end.y.toFloat())
        } else {
            selectionRect[0].pos = Vector2f(start.x.toFloat(), end.y.toFloat())
            selectionRect[1].pos = Vector2f(end.x.toFloat(), end.y.toFloat())
            selectionRect[2].pos = Vector2f(start.x.toFloat(), start.y.toFloat())
            selectionRect[3].pos = Vector2f(end.x.toFloat(), start.y.toFloat())
        }
    }

    fun getIndexesCount(): Int {
        return indexes.size
    }

}