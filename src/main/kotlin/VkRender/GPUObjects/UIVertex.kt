package VkRender.GPUObjects

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f
import org.joml.Vector4fc
import java.nio.ByteBuffer

class UIVertex(
    var pos: Vector2fc,
    var color: Vector4fc,
) : GPUObject() {

    companion object : GPUObjectAutoProperties(Vector2f(), Vector4f());

    override fun put(buffer: ByteBuffer) {
        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(color.x())
        buffer.putFloat(color.y())
        buffer.putFloat(color.z())
        buffer.putFloat(color.w())
    }
}