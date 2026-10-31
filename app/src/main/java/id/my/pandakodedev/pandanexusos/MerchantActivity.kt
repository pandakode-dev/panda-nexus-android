package id.my.pandakodedev.pandanexusos

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MerchantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val api = NexusApi.create()
    private lateinit var session: SessionManager
    private var tts: TextToSpeech? = null

    private var activeTab = "kanban" // "kanban", "pos", "stats", "theme"
    private var branchId = 1
    private var branchName = "Cabang Utama"
    private var prefix = "LND"
    private var stages: List<String> = listOf("📥 Masuk", "🫧 Proses", "✨ Finishing", "🛍️ Selesai")
    private var queues: List<QueueData> = emptyList()
    private var catalog: List<CatalogData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        tts = TextToSpeech(this, this)
        loadMerchantDashboard()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("id", "ID")
        }
    }

    private fun speakQueue(code: String, name: String, stage: String) {
        tts?.speak("Nomor antrian $code, atas nama $name, status saat ini $stage", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun loadMerchantDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = api.getMerchantData(session.getUserId())
                branchId = res.branch_id
                branchName = res.branch_name
                prefix = res.prefix
                stages = res.stages
                queues = res.queues
                catalog = res.catalog
                withContext(Dispatchers.Main) { renderMerchantUI() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    renderMerchantUI()
                    Toast.makeText(this@MerchantActivity, "Gagal sinkronisasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderMerchantUI() {
        val pal = UiKit.getPalette(session.getTheme())
        val scroll = ScrollView(this).apply { setBackgroundColor(pal.bgSoft) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 70)
        }

        // 1. Banner Ornamen Gradasi + Kanvas 3D Mini
        val banner = UiKit.gradientBanner(this, pal.primary, pal.accent)
        banner.addView(UiKit.pillBadge(this, "👑 NATIVE MERCHANT POS & QUEUE OS", Color.parseColor("#33000000")))

        banner.addView(TextView(this).apply {
            text = "🏪 ${session.getBusinessName()}"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        banner.addView(TextView(this).apply {
            text = "📍 $branchName • Pemilik: ${session.getFullName()}"
            textSize = 13f
            setTextColor(Color.parseColor("#f0f9ff"))
            setPadding(0, 4, 0, 14)
        })

        // Animasi 3D Interaktif di Dalam Header Mitra
        banner.addView(Nexus3DView(this, pal.primary, pal.accent).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 340)
        })
        root.addView(banner)

        // 2. Bar Navigasi 4 Menu Berwarna (Kanban, Kasir POS, Omzet & Stok, 12 Tema)
        val navGrid1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        navGrid1.addView(UiKit.button3D(this, "🎟️ Antrian (${queues.size})", pal.primary, pal.accent) {
            activeTab = "kanban"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { rightMargin = 10; bottomMargin = 10 } })

        navGrid1.addView(UiKit.button3D(this, "🛒 Kasir POS (${catalog.size})", Color.parseColor("#ea580c"), Color.parseColor("#fb923c")) {
            activeTab = "pos"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { bottomMargin = 10 } })
        root.addView(navGrid1)

        val navGrid2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 24 }
        }
        navGrid2.addView(UiKit.button3D(this, "📊 Omzet & Stok", Color.parseColor("#059669"), Color.parseColor("#34d399")) {
            activeTab = "stats"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { rightMargin = 10 } })

        navGrid2.addView(UiKit.button3D(this, "🎨 12 Tema Warna", Color.parseColor("#db2777"), Color.parseColor("#f472b6")) {
            activeTab = "theme"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f) })
        root.addView(navGrid2)

        // 3. Render Tab Aktif
        when (activeTab) {
            "kanban" -> renderKanbanTab(root, pal)
            "pos"    -> renderPosTab(root, pal)
            "stats"  -> renderStatsAndStockTab(root, pal)
            "theme"  -> renderThemeTab(root, pal)
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun renderKanbanTab(root: LinearLayout, pal: UiKit.Palette) {
        val actionTop = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actionTop.addView(UiKit.button3D(this, "➕ Input Antrian Baru", pal.primary, pal.accent) {
            showQuickQueueDialog(pal)
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 125, 1.4f).apply { rightMargin = 10; bottomMargin = 20 } })

        actionTop.addView(UiKit.button3D(this, "🔄 Refresh", Color.parseColor("#475569"), Color.parseColor("#64748b")) {
            loadMerchantDashboard()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 125, 0.8f).apply { bottomMargin = 20 } })
        root.addView(actionTop)

        stages.forEachIndexed { idx, stageTitle ->
            val stageQueues = queues.filter { it.current_stage == idx }
            root.addView(UiKit.pillBadge(this, "Tahap ${idx + 1}: $stageTitle (${stageQueues.size} Antrian)", pal.primary))

            if (stageQueues.isEmpty()) {
                root.addView(TextView(this).apply {
                    text = "✨ Belum ada antrian di tahap ini."
                    textSize = 13f
                    setTextColor(Color.GRAY)
                    setPadding(16, 0, 16, 26)
                })
            }

            stageQueues.forEach { q ->
                val qCard = UiKit.card3D(this, pal.border)

                // Header Tiket
                qCard.addView(TextView(this).apply {
                    text = "🎟️ ${q.queue_code} — ${q.customer_name}"
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(pal.primary)
                })

                // Visual Progress Bar 4 Tahap
                qCard.addView(UiKit.stageProgressBar(this, idx, stages.size, pal.primary))

                qCard.addView(TextView(this).apply {
                    text = "📌 Detail: ${q.customer_ref}\n🛍️ Layanan: ${q.items_summary}\n💰 Estimasi: Rp ${q.estimated_price.toInt()}"
                    textSize = 14f
                    setTextColor(Color.parseColor("#1e293b"))
                    setPadding(0, 6, 0, 18)
                })

                // Baris Tombol 1: Panggil Suara, Cetak Struk Bluetooth, Kirim WA
                val toolRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                toolRow.addView(UiKit.button3D(this, "🔊 Suara", Color.parseColor("#0d9488"), Color.parseColor("#2dd4bf")) {
                    speakQueue(q.queue_code, q.customer_name, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 1f).apply { rightMargin = 8; bottomMargin = 10 } })

                toolRow.addView(UiKit.button3D(this, "🖨️ Print BT", Color.parseColor("#4338ca"), Color.parseColor("#6366f1")) {
                    BluetoothPrinter.printQueueTicket(this@MerchantActivity, session.getBusinessName(), branchName, q, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 1.1f).apply { rightMargin = 8; bottomMargin = 10 } })

                toolRow.addView(UiKit.button3D(this, "📲 WA", Color.parseColor("#15803d"), Color.parseColor("#22c55e")) {
                    BluetoothPrinter.shareViaWhatsApp(this@MerchantActivity, session.getBusinessName(), q, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 0.9f).apply { bottomMargin = 10 } })
                qCard.addView(toolRow)

                // Baris Tombol 2: Geser Tahap Selanjutnya / Selesai
                if (idx + 1 < stages.size) {
                    qCard.addView(UiKit.button3D(this, "Geser ke: ${stages[idx + 1]} ➔", pal.primary, pal.accent) {
                        updateQueueStatus(q.id, idx + 1, 0)
                    })
                } else {
                    qCard.addView(UiKit.button3D(this, "✅ Selesai & Masukkan ke Omzet", Color.parseColor("#059669"), Color.parseColor("#10b981")) {
                        updateQueueStatus(q.id, idx, 1)
                    })
                }

                root.addView(qCard)
            }
        }
    }

    private fun renderPosTab(root: LinearLayout, pal: UiKit.Palette) {
        root.addView(UiKit.button3D(this, "➕ Tambah Layanan / Barang Stok Baru", Color.parseColor("#059669"), Color.parseColor("#34d399")) {
            showAddCatalogDialog(pal)
        })

        val icons = listOf("🧺", "🧥", "👟", "🛢️", "🔧", "🍔", "☕", "🍜", "💈", "📦")
        catalog.forEachIndexed { i, item ->
            val card = UiKit.card3D(this, pal.border)
            val emoji = icons[i % icons.size]

            card.addView(UiKit.pillBadge(this, item.category.uppercase(), pal.accent))
            card.addView(TextView(this).apply {
                text = "$emoji ${item.item_name}"
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#0f172a"))
            })
            card.addView(TextView(this).apply {
                text = "💰 Rp ${item.price.toInt()} / ${item.unit}   •   📦 Stok: ${item.stock} ${item.unit}"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (item.stock <= 10) Color.parseColor("#e11d48") else pal.primary)
                setPadding(0, 8, 0, 16)
            })

            card.addView(UiKit.button3D(this, "🛒 Pilih Item, Potong Stok & Buat Antrian", pal.primary, pal.accent) {
                showPosCheckoutDialog(item, pal)
            })
            root.addView(card)
        }
    }

    private fun renderStatsAndStockTab(root: LinearLayout, pal: UiKit.Palette) {
        val activePotential = queues.sumOf { it.estimated_price }.toInt()
        val lowStockCount = catalog.count { it.stock <= 15 }

        val summaryCard = UiKit.card3D(this, Color.parseColor("#a7f3d0"), Color.parseColor("#ecfdf5"))
        summaryCard.addView(TextView(this).apply {
            text = "💰 Ringkasan Omzet & Stok Gudang"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#065f46"))
        })
        summaryCard.addView(TextView(this).apply {
            text = "• Potensi Antrian Berjalan: Rp $activePotential (${queues.size} Pesanan)\n• Peringatan Stok Menipis: $lowStockCount Item"
            textSize = 14f
            setPadding(0, 10, 0, 6)
        })
        root.addView(summaryCard)

        root.addView(TextView(this).apply {
            text = "📦 Kontrol Stok Gudang (Klik +25 untuk Restock Instan)"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 8, 8, 16)
        })

        catalog.forEach { item ->
            val isLow = item.stock <= 15
            val stCard = UiKit.card3D(
                this,
                if (isLow) Color.parseColor("#fda4af") else pal.border,
                if (isLow) Color.parseColor("#fff1f2") else Color.WHITE
            )
            stCard.addView(TextView(this).apply {
                text = "${if (isLow) "🚨 STOK MENIPIS: " else "✅ "}${item.item_name}"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            })
            stCard.addView(TextView(this).apply {
                text = "Sisa di Gudang: ${item.stock} ${item.unit}"
                textSize = 14f
                setPadding(0, 6, 0, 14)
            })
            stCard.addView(UiKit.button3D(this, "➕ Tambah Stok +25 ${item.unit}", pal.primary, pal.accent) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        api.restockItem(session.getUserId(), item.id, 25)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MerchantActivity, "Stok ${item.item_name} ditambah +25!", Toast.LENGTH_SHORT).show()
                            loadMerchantDashboard()
                        }
                    } catch (e: Exception) {}
                }
            })
            root.addView(stCard)
        }
    }

    private fun renderThemeTab(root: LinearLayout, pal: UiKit.Palette) {
        val card = UiKit.card3D(this, pal.border)
        card.addView(TextView(this).apply {
            text = "🎨 12 Kombinasi Warna Cerah & Elegan"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        })

        UiKit.getAllThemes().forEach { (key, tPal) ->
            card.addView(UiKit.button3D(this, tPal.name, tPal.primary, tPal.accent) {
                session.setTheme(key)
                renderMerchantUI()
            })
        }

        card.addView(UiKit.button3D(this, "🚪 Keluar dari Akun Mitra (Logout)", Color.parseColor("#e11d48"), Color.parseColor("#fb7185")) {
            session.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })
        root.addView(card)
    }

    private fun showAddCatalogDialog(pal: UiKit.Palette) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }
        val etName = UiKit.inputModern(this, "Nama Layanan / Barang", pal.border)
        val etCat  = UiKit.inputModern(this, "Kategori (Jasa / Sparepart / Menu)", pal.border)
        val etPrice = UiKit.inputModern(this, "Harga Satuan (Rp)", pal.border)
        val etStock = UiKit.inputModern(this, "Stok Awal (Misal: 50)", pal.border)
        val etUnit  = UiKit.inputModern(this, "Satuan (kg / pcs / botol / porsi)", pal.border)
        box.addView(etName); box.addView(etCat); box.addView(etPrice); box.addView(etStock); box.addView(etUnit)

        AlertDialog.Builder(this)
            .setTitle("➕ Tambah Item Katalog & Stok")
            .setView(box)
            .setPositiveButton("Simpan ke Gudang") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        api.addCatalogItem(
                            session.getUserId(),
                            etName.text.toString(),
                            etCat.text.toString(),
                            etPrice.text.toString(),
                            etStock.text.toString(),
                            etUnit.text.toString()
                        )
                        withContext(Dispatchers.Main) { loadMerchantDashboard() }
                    } catch (e: Exception) {}
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateQueueStatus(queueId: Int, nextStage: Int, isCompleted: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                api.updateQueue(mapOf("queue_id" to queueId, "next_stage" to nextStage, "is_completed" to isCompleted))
                withContext(Dispatchers.Main) { loadMerchantDashboard() }
            } catch (e: Exception) {}
        }
    }

    private fun showQuickQueueDialog(pal: UiKit.Palette) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }
        val etName = UiKit.inputModern(this, "👤 Nama Pelanggan", pal.border)
        val etRef  = UiKit.inputModern(this, "📌 Berat Kg / Plat Nomor / No Meja", pal.border)
        val etItem = UiKit.inputModern(this, "🛍️ Rincian Pesanan", pal.border)
        val etPrice = UiKit.inputModern(this, "💰 Perkiraan Harga (Rp)", pal.border)
        box.addView(etName); box.addView(etRef); box.addView(etItem); box.addView(etPrice)

        AlertDialog.Builder(this)
            .setTitle("➕ Buat Antrian Baru")
            .setView(box)
            .setPositiveButton("Simpan & Masuk Antrian") { _, _ ->
                createOrderApi(etName.text.toString(), etRef.text.toString(), etItem.text.toString(), etPrice.text.toString(), 0)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPosCheckoutDialog(item: CatalogData, pal: UiKit.Palette) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }
        val etName = UiKit.inputModern(this, "👤 Nama Pelanggan", pal.border)
        val etRef  = UiKit.inputModern(this, "📌 Catatan (Kg / Plat Nomor / Meja)", pal.border)
        val etQty  = UiKit.inputModern(this, "🔢 Jumlah (${item.unit})", pal.border).apply { setText("1") }
        box.addView(etName); box.addView(etRef); box.addView(etQty)

        AlertDialog.Builder(this)
            .setTitle("🧾 Kasir POS: ${item.item_name}")
            .setView(box)
            .setPositiveButton("Proses & Buat Antrian") { _, _ ->
                val qty = etQty.text.toString().toIntOrNull() ?: 1
                val totalPrice = (item.price * qty).toInt().toString()
                createOrderApi(etName.text.toString(), etRef.text.toString(), "${item.item_name} (x$qty)", totalPrice, item.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createOrderApi(name: String, ref: String, summary: String, price: String, catalogId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = api.createQueue(mapOf(
                    "user_id" to session.getUserId().toString(),
                    "branch_id" to branchId.toString(),
                    "prefix" to prefix,
                    "customer_name" to name,
                    "customer_ref" to ref,
                    "items_summary" to summary,
                    "estimated_price" to price,
                    "catalog_id" to catalogId.toString()
                ))
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MerchantActivity, "🎉 Antrian Dibuat: ${res.queue_code}", Toast.LENGTH_LONG).show()
                    loadMerchantDashboard()
                }
            } catch (e: Exception) {}
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}