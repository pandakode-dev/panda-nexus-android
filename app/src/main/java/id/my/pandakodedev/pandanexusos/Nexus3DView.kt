package id.my.pandakodedev.pandanexusos

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class Nexus3DView(
    context: Context,
    private var primaryColor: Int = Color.parseColor("#0284c7"),
    private var accentColor: Int = Color.parseColor("#38bdf8")
) : View(context) {

    private var rotX = 0.25f
    private var rotY = 0.45f
    private var tick = 0f
    private var lastX = 0f
    private var lastY = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private data class OrbitNode(val emoji: String, val angle: Float, val yOff: Float, val color: Int)

    private val icons = listOf(
        OrbitNode("🧺", 0.0f, -28f, Color.parseColor("#0284c7")),
        OrbitNode("🏎️", 1.05f, 28f, Color.parseColor("#ea580c")),
        OrbitNode("🍔", 2.10f, -28f, Color.parseColor("#db2777")),
        OrbitNode("💈", 3.14f, 28f, Color.parseColor("#7c3aed")),
        OrbitNode("🛒", 4.19f, -28f, Color.parseColor("#059669")),
        OrbitNode("🎟️", 5.23f, 28f, Color.parseColor("#d97706"))
    )

    private data class RenderPoint(
        val sx: Float,
        val sy: Float,
        val z: Float,
        val scale: Float,
        val emoji: String,
        val color: Int
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                rotY += (event.x - lastX) * 0.012f
                rotX += (event.y - lastY) * 0.012f
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        tick += 0.035f
        rotY += 0.014f

        // 1. Ornamen Lingkaran Halo Berwarna di Latar
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, width * 0.42f,
            intArrayOf(accentColor and 0x44FFFFFF, Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, width * 0.42f, paint)
        paint.shader = null

        // 2. Proyeksi 3D Titik Orbit
        val radius = width.coerceAtMost(height) * 0.34f
        val projected = icons.mapIndexed { idx, node ->
            val a = node.angle + rotY
            val x = cos(a) * radius
            val z = sin(a) * radius
            val y = node.yOff + sin(tick + idx) * 14f

            val y2 = y * cos(rotX) - z * sin(rotX)
            val z2 = z * cos(rotX) + y * sin(rotX)
            val scale = 380f / (380f + z2)

            RenderPoint(cx + x * scale, cy + y2 * scale, z2, scale, node.emoji, node.color)
        }.sortedByDescending { it.z }

        // 3. Garis Sinar Kristal 3D ke Inti
        projected.forEach { p ->
            paint.color = p.color
            paint.alpha = 70
            paint.strokeWidth = 4f
            canvas.drawLine(cx, cy, p.sx, p.sy, paint)
        }

        // 4. Bola Inti Panda 3D Bersinar di Tengah
        val coreFloat = sin(tick) * 8f
        paint.alpha = 255
        paint.shader = RadialGradient(
            cx - 15f, cy + coreFloat - 15f, 68f,
            intArrayOf(Color.WHITE, primaryColor, accentColor),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy + coreFloat, 64f, paint)
        paint.shader = null

        textPaint.textSize = 54f
        canvas.drawText("🐼", cx, cy + coreFloat + 18f, textPaint)

        // 5. Kapsul Ikon 3D Mengorbit
        projected.forEach { p ->
            val bubbleR = 44f * p.scale
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawCircle(p.sx, p.sy, bubbleR, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f * p.scale
            paint.color = p.color
            canvas.drawCircle(p.sx, p.sy, bubbleR, paint)

            textPaint.textSize = 40f * p.scale
            canvas.drawText(p.emoji, p.sx, p.sy + (14f * p.scale), textPaint)
        }

        postInvalidateOnAnimation()
    }
}