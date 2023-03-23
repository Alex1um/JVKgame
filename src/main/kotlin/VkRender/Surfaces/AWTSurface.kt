package VkRender.Surfaces

import VkRender.Instance
import VkRender.Util
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import org.lwjgl.system.jawt.JAWT
import org.lwjgl.system.jawt.JAWTFunctions
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo
import org.lwjgl.system.jawt.JAWTX11DrawingSurfaceInfo
import org.lwjgl.system.windows.WinBase
import org.lwjgl.vulkan.*
import java.awt.AWTException
import java.awt.Canvas
import java.nio.ByteBuffer

class AWTSurface(private val canvas: Canvas, override val instance: Instance) : Surface {

    override var surface: Long = 0L

    companion object {
        private val awt: JAWT = JAWT.calloc()

        init {
            awt.version(JAWTFunctions.JAWT_VERSION_1_4)
            if (!JAWTFunctions.JAWT_GetAWT(awt)) throw AssertionError("GetAWT failed")
        }
    }

    init {
        with(Util) {
            MemoryStack.stackPush().use { stack ->
                val ds = JAWTFunctions.JAWT_GetDrawingSurface(canvas, awt.GetDrawingSurface())
                surface = try {
                    val lock = JAWTFunctions.JAWT_DrawingSurface_Lock(ds!!, ds.Lock())
                    if (lock and JAWTFunctions.JAWT_LOCK_ERROR != 0) throw AWTException("JAWT_DrawingSurface_Lock() failed")
                    try {
                        val dsi =
                            JAWTFunctions.JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds, ds.GetDrawingSurfaceInfo())
                        try {
                            val err = when (Platform.get()) {
                                Platform.WINDOWS -> {
                                    val dsiWin = JAWTWin32DrawingSurfaceInfo.create(dsi!!.platformInfo())
                                    val hwnd = dsiWin.hwnd()
                                    val info = VkWin32SurfaceCreateInfoKHR.calloc(stack)
                                        .sType(KHRWin32Surface.VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR)
                                        .hinstance(WinBase.GetModuleHandle(null as ByteBuffer?))
                                        .hwnd(hwnd)
                                    KHRWin32Surface.vkCreateWin32SurfaceKHR(
                                        instance.instance,
                                        info,
                                        null,
                                        lp
                                    )
                                }
                                Platform.LINUX -> {
                                    val dsiX11 = JAWTX11DrawingSurfaceInfo.create(dsi!!.platformInfo())
                                    val display = dsiX11.display()
                                    val window = dsiX11.drawable()

                                    val info = VkXlibSurfaceCreateInfoKHR.calloc(stack)
                                        .pNext(MemoryUtil.NULL)
                                        .sType(KHRXlibSurface.VK_STRUCTURE_TYPE_XLIB_SURFACE_CREATE_INFO_KHR)
                                        .dpy(display)
                                        .window(window)

                                    KHRXlibSurface.vkCreateXlibSurfaceKHR(instance.instance, info, null, lp)

                                }
                                else -> throw IllegalStateException("Platform is not supported")
                            }
                            if (err != VK13.VK_SUCCESS) {
                                throw AWTException("Calling vkCreateXlibSurfaceKHR failed with error: $err")
                            }
                            lp[0]
                        } finally {
                            JAWTFunctions.JAWT_DrawingSurface_FreeDrawingSurfaceInfo(dsi!!, ds.FreeDrawingSurfaceInfo())
                        }
                    } finally {
                        JAWTFunctions.JAWT_DrawingSurface_Unlock(ds, ds.Unlock())
                    }
                } finally {
                    JAWTFunctions.JAWT_FreeDrawingSurface(ds!!, awt.FreeDrawingSurface())
                }
            }
        }
    }
}