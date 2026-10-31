package id.my.pandakodedev.pandanexusos

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.*

object UiKit {

    data class Palette(val name: String, val primary: Int, val accent: Int, val bgSoft: Int, val border: Int)

    fun getAllThemes(): Map<String, Palette> = linkedMapOf(
        "sky_white"         to Palette("💙 Sky Blue & White", Color.parseColor("#0284c7"), Color.parseColor("#38bdf8"), Color.parseColor("#f0f9ff"), Color.parseColor("#bae6fd")),
        "yellow_white"      to Palette("💛 Sun Yellow & White", Color.parseColor("#d97706"), Color.parseColor("#facc15"), Color.parseColor("#fefce8"), Color.parseColor("#fde047")),
        "pink_white"        to Palette("💗 Sakura Pink & White", Color.parseColor("#db2777"), Color.parseColor("#f472b6"), Color.parseColor("#fdf2f8"), Color.parseColor("#fbcfe8")),
        "orange_white"      to Palette("🧡 Tangerine & White", Color.parseColor("#ea580c"), Color.parseColor("#fb923c"), Color.parseColor("#fff7ed"), Color.parseColor("#fed7aa")),
        "mint_white"        to Palette("💚 Fresh Mint & White", Color.parseColor("#059669"), Color.parseColor("#34d399"), Color.parseColor("#ecfdf5"), Color.parseColor("#a7f3d0")),
        "lavender_white"    to Palette("💜 Royal Purple & White", Color.parseColor("#7c3aed"), Color.parseColor("#a78bfa"), Color.parseColor("#f5f3ff"), Color.parseColor("#ddd6fe")),
        "coral_white"       to Palette("❤️ Ruby Coral & White", Color.parseColor("#e11d48"), Color.parseColor("#fb7185"), Color.parseColor("#fff1f2"), Color.parseColor("#fecdd3")),
        "tosca_white"       to Palette("🩵 Aqua Tosca & White", Color.parseColor("#0d9488"), Color.parseColor("#2dd4bf"), Color.parseColor("#f0fdfa"), Color.parseColor("#99f6e4")),
        "berry_gold"        to Palette("🍓 Berry Gold & White", Color.parseColor("#be185d"), Color.parseColor("#f59e0b"), Color.parseColor("#fff1f2"), Color.parseColor("#fbcfe8")),
        "ocean_lime"        to Palette("🌊 Ocean Lime & White", Color.parseColor("#0369a1"), Color.parseColor("#84cc16"), Color.parseColor("#f0f9ff"), Color.parseColor("#bae6fd")),
        "sunset_peach"      to Palette("🌅 Sunset Peach & White", Color.parseColor("#c2410c"), Color.parseColor("#f43f5e"), Color.parseColor("#fff7ed"), Color.parseColor("#fed7aa")),
        "indigo_cyan"       to Palette("🌌 Electric Indigo & White", Color.parseColor("#4338ca"), Color.parseColor("#06b6d4"), Color.parseColor("#eef2ff"), Color.parseColor("#c7d2fe")),
        "matcha_latte"      to Palette("🍵 Matcha Cafe & White", Color.parseColor("#3f6212"), Color.parseColor("#84cc16"), Color.parseColor("#f7fee7"), Color.parseColor("#d9f99d")),
        "caramel_coffee"    to Palette("☕ Caramel Bistro & White", Color.parseColor("#92400e"), Color.parseColor("#d97706"), Color.parseColor("#fffbeb"), Color.parseColor("#fde68a")),
        "rose_gold"         to Palette("🌸 Rose Gold Luxury", Color.parseColor("#be123c"), Color.parseColor("#f43f5e"), Color.parseColor("#fff1f2"), Color.parseColor("#fecdd3")),
        "royal_emerald"     to Palette("💎 Royal Emerald & Gold", Color.parseColor("#065f46"), Color.parseColor("#10b981"), Color.parseColor("#ecfdf5"), Color.parseColor("#6ee7b7")),
        "velvet_plum"       to Palette("🍇 Velvet Plum & White", Color.parseColor("#6b21a8"), Color.parseColor("#d946ef"), Color.parseColor("#faf5ff"), Color.parseColor("#e9d5ff")),
        "cyber_neon"        to Palette("⚡ Cyber Blue & Magenta", Color.parseColor("#1d4ed8"), Color.parseColor("#ec4899"), Color.parseColor("#eff6ff"), Color.parseColor("#bfdbfe")),
        "nordic_frost"      to Palette("❄️ Nordic Ice & Slate", Color.parseColor("#0f172a"), Color.parseColor("#0284c7"), Color.parseColor("#f8fafc"), Color.parseColor("#cbd5e1")),
        "flamingo_tropical" to Palette("🦩 Tropical Flamingo", Color.parseColor("#db2777"), Color.parseColor("#06b6d4"), Color.parseColor("#fdf2f8"), Color.parseColor("#fbcfe8"))
    )

    fun getPalette(themeKey: String): Palette {
        val map = getAllThemes()
        return map[themeKey] ?: map["sky_white"]!!
    }

    fun card3D(ctx: Context, borderColor: Int, bgColor: Int = Color.WHITE): LinearLayout {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(42, 42, 42, 42)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 52f
                setColor(bgColor)
                setStroke(5, borderColor)
            }
            elevation = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
    }

    fun gradientBanner(ctx: Context, colorStart: Int, colorEnd: Int): LinearLayout {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(46, 46, 46, 46)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(colorStart, colorEnd)
            ).apply { cornerRadius = 56f }
            elevation = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 34 }
        }
    }

    fun pillBadge(ctx: Context, textStr: String, bgColor: Int, textColor: Int = Color.WHITE): TextView {
        return TextView(ctx).apply {
            text = textStr
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            setPadding(28, 12, 28, 12)
            background = GradientDrawable().apply {
                cornerRadius = 99f
                setColor(bgColor)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 14; rightMargin = 12 }
        }
    }

    fun stageProgressBar(ctx: Context, currentStage: Int, totalStages: Int, activeColor: Int): LinearLayout {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }

            for (i in 0 until totalStages) {
                val bar = View(ctx).apply {
                    background = GradientDrawable().apply {
                        cornerRadius = 20f
                        setColor(if (i <= currentStage) activeColor else Color.parseColor("#e2e8f0"))
                    }
                    layoutParams = LinearLayout.LayoutParams(0, 22, 1f).apply {
                        rightMargin = if (i < totalStages - 1) 12 else 0
                    }
                }
                addView(bar)
            }
        }
    }

    fun button3D(ctx: Context, label: String, colorStart: Int, colorEnd: Int, onClick: () -> Unit): Button {
        return Button(ctx).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(colorStart, colorEnd)
            ).apply { cornerRadius = 38f }
            elevation = 10f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                130
            ).apply { bottomMargin = 18 }
            setOnClickListener { onClick() }
        }
    }

    fun inputModern(ctx: Context, hintText: String, borderColor: Int, isPassword: Boolean = false): EditText {
        return EditText(ctx).apply {
            hint = hintText
            textSize = 15f
            setPadding(38, 32, 38, 32)
            if (isPassword) {
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            background = GradientDrawable().apply {
                cornerRadius = 34f
                setColor(Color.WHITE)
                setStroke(4, borderColor)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
    }
}