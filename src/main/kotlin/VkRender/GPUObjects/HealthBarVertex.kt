package VkRender.GPUObjects

import org.joml.Vector2f
import org.joml.Vector2fc
import java.nio.ByteBuffer

class HealthBarVertex(
    var pos: Vector2fc,
    var healthSplitX: Float,
    var healthPercent: Float,
) : GPUObject() {

    companion object : GPUObjectAutoProperties(Vector2f(), Float, Float)

    override fun put(buffer: ByteBuffer) {

        buffer.putFloat(pos.x())
        buffer.putFloat(pos.y())

        buffer.putFloat(healthSplitX)
        buffer.putFloat(healthPercent)
    }


}