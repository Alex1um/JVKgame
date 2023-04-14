package VkRender.Textures

import VkRender.CommandPool
import VkRender.Device
import VkRender.PhysicalDevice
import java.io.Closeable

class Images(vararg paths: String) : Closeable {

    lateinit var textures: List<TextureImage>

    private val tmpPaths = paths

    val size = paths.size

    fun init(ldevice: Device, pdevice: PhysicalDevice, commands: CommandPool) {
        textures = tmpPaths.map { path ->
            TextureImage(ldevice, pdevice, commands, path)
        }
    }

    override fun close() {
        for (texture in textures) {
            texture.close()
        }
    }

}