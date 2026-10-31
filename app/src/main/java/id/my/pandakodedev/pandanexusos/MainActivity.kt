package id.my.pandakodedev.pandanexusos

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val api = NexusApi.create()
    private lateinit var session: SessionManager
    private var tts: TextToSpeech? = null

    private var activeTab = "kanban" // "kanban", "pos", "stats", "theme"
    private var branchId = 1
    private var branchName = "Cabang Utama"
    private var prefix = "FNB"
    private var taxPercent = 10.0
    private var servicePercent = 0.0
    private var stages: List<String> = listOf("🧾 Antrian", "🍳 Proses", "🍱 Packing", "🛎️ Siap")
    private var queues: List<QueueData> = emptyList()
    private var catalog: List<CatalogData> = emptyList()

    // Keranjang Belanja Multi-Item POS
    data class PosCartItem(val item: CatalogData, var qty: Int)
    private val posCart = mutableListOf<PosCartItem>()

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
        tts?.speak("Nomor antrian $code, atas nama $name, status $stage", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun loadMerchantDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = api.getMerchantData(session.getUserId())
                branchId = res.branch_id
                branchName = res.branch_name
                taxPercent = res.tax_percent
                servicePercent = res.service_percent
                prefix = res.prefix
                stages = res.stages
                queues = res.queues
                catalog = res.catalog
                session.setTheme(res.selected_theme)
                withContext(Dispatchers.Main) { renderMerchantUI() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    renderMerchantUI()
                    Toast.makeText(this@MainActivity, "Gagal sinkronisasi: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // 1. Banner Ornamen Gradasi + Kanvas 3D
        val banner = UiKit.gradientBanner(this, pal.primary, pal.accent)
        banner.addView(UiKit.pillBadge(this, "👑 MERCHANT POS • PAJAK ${taxPercent}%", Color.parseColor("#33000000")))
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
            setPadding(0, 4, 0, 12)
        })
        banner.addView(Nexus3DView(this, pal.primary, pal.accent).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300)
        })

        if (session.getRole() == "superadmin") {
            banner.addView(UiKit.button3D(this, "🛡️ Buka Panel Master SuperAdmin", Color.parseColor("#0f172a"), Color.parseColor("#334155")) {
                startActivity(Intent(this, SuperAdminActivity::class.java))
            })
        }
        root.addView(banner)

        // 2. Navigasi 4 Tab
        val nav1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        nav1.addView(UiKit.button3D(this, "🎟️ Antrian (${queues.size})", pal.primary, pal.accent) {
            activeTab = "kanban"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { rightMargin = 10; bottomMargin = 10 } })

        nav1.addView(UiKit.button3D(this, "🍔 Menu & POS (${posCart.sumOf { it.qty }})", Color.parseColor("#ea580c"), Color.parseColor("#fb923c")) {
            activeTab = "pos"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { bottomMargin = 10 } })
        root.addView(nav1)

        val nav2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 24 }
        }
        nav2.addView(UiKit.button3D(this, "📊 Pajak & Stok", Color.parseColor("#059669"), Color.parseColor("#34d399")) {
            activeTab = "stats"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { rightMargin = 10 } })

        nav2.addView(UiKit.button3D(this, "🎨 20 Tema", Color.parseColor("#db2777"), Color.parseColor("#f472b6")) {
            activeTab = "theme"; renderMerchantUI()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f) })
        root.addView(nav2)

        when (activeTab) {
            "kanban" -> renderKanbanTab(root, pal)
            "pos"    -> renderPosTab(root, pal)
            "stats"  -> renderStatsAndTaxTab(root, pal)
            "theme"  -> renderThemeTab(root, pal)
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun renderKanbanTab(root: LinearLayout, pal: UiKit.Palette) {
        stages.forEachIndexed { idx, stageTitle ->
            val stageQueues = queues.filter { it.current_stage == idx }
            root.addView(UiKit.pillBadge(this, "Tahap ${idx + 1}: $stageTitle (${stageQueues.size})", pal.primary))

            stageQueues.forEach { q ->
                val qCard = UiKit.card3D(this, pal.border)
                qCard.addView(TextView(this).apply {
                    text = "🎟️ ${q.queue_code} — ${q.customer_name}"
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(pal.primary)
                })
                qCard.addView(UiKit.stageProgressBar(this, idx, stages.size, pal.primary))

                val sub = if (q.subtotal_price > 0) q.subtotal_price.toInt() else q.estimated_price.toInt()
                qCard.addView(TextView(this).apply {
                    text = "📌 ${q.customer_ref}\n🛍️ ${q.items_summary}\n💵 Subtotal: Rp $sub  |  🧾 Pajak: +Rp ${q.tax_amount.toInt()}\n💰 TOTAL BAYAR: Rp ${q.estimated_price.toInt()}"
                    textSize = 14f
                    setPadding(0, 6, 0, 16)
                })

                val toolRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                toolRow.addView(UiKit.button3D(this, "🔊 Suara", Color.parseColor("#0d9488"), Color.parseColor("#2dd4bf")) {
                    speakQueue(q.queue_code, q.customer_name, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 1f).apply { rightMargin = 8; bottomMargin = 10 } })

                toolRow.addView(UiKit.button3D(this, "🖨️ Print BT", Color.parseColor("#4338ca"), Color.parseColor("#6366f1")) {
                    BluetoothPrinter.printQueueTicket(this@MainActivity, session.getBusinessName(), branchName, q, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 1.1f).apply { rightMargin = 8; bottomMargin = 10 } })

                toolRow.addView(UiKit.button3D(this, "📲 WA", Color.parseColor("#15803d"), Color.parseColor("#22c55e")) {
                    BluetoothPrinter.shareViaWhatsApp(this@MainActivity, session.getBusinessName(), q, stageTitle)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 0.9f).apply { bottomMargin = 10 } })
                qCard.addView(toolRow)

                if (idx + 1 < stages.size) {
                    qCard.addView(UiKit.button3D(this, "Lanjut ke: ${stages[idx + 1]} ➔", pal.primary, pal.accent) {
                        updateQueueStatus(q.id, idx + 1, 0)
                    })
                } else {
                    qCard.addView(UiKit.button3D(this, "✅ Selesai & Masuk Omzet", Color.parseColor("#059669"), Color.parseColor("#10b981")) {
                        updateQueueStatus(q.id, idx, 1)
                    })
                }
                root.addView(qCard)
            }
        }
    }

    // TAB KASIR POS MULTI-ITEM + KALKULATOR PAJAK & DISKON
    private fun renderPosTab(root: LinearLayout, pal: UiKit.Palette) {
        // 1. Kotak Keranjang Pesanan Aktif & Rincian Pajak
        val cartCard = UiKit.card3D(this, pal.primary, Color.WHITE)
        cartCard.addView(TextView(this).apply {
            text = "🛒 Keranjang Pesanan & Kalkulator Pajak (${posCart.sumOf { it.qty }} Item)"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(pal.primary)
        })

        if (posCart.isEmpty()) {
            cartCard.addView(TextView(this).apply {
                text = "Belum ada menu/produk dipilih. Klik tombol '+ Tambah Pesanan' pada daftar menu di bawah."
                textSize = 13f
                setTextColor(Color.GRAY)
                setPadding(0, 10, 0, 10)
            })
        } else {
            val subtotal = posCart.sumOf { it.item.price * it.qty }
            val taxVal = (subtotal * ((taxPercent + servicePercent) / 100.0)).roundToInt()
            val grandTotal = subtotal.toInt() + taxVal

            posCart.forEachIndexed { idx, c ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 10, 0, 10)
                }
                row.addView(TextView(this).apply {
                    text = "${c.item.item_name}\n${c.qty} ${c.item.unit} x Rp ${c.item.price.toInt()}"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                })
                row.addView(UiKit.button3D(this, "➖", Color.parseColor("#64748b"), Color.parseColor("#94a3b8")) {
                    if (c.qty > 1) c.qty-- else posCart.removeAt(idx)
                    renderMerchantUI()
                }.apply { layoutParams = LinearLayout.LayoutParams(110, 100).apply { rightMargin = 8 } })

                row.addView(UiKit.button3D(this, "➕", pal.primary, pal.accent) {
                    if (c.qty < c.item.stock) c.qty++
                    renderMerchantUI()
                }.apply { layoutParams = LinearLayout.LayoutParams(110, 100) })

                cartCard.addView(row)
            }

            cartCard.addView(TextView(this).apply {
                text = "--------------------------------------------\n" +
                        "💵 Subtotal Menu : Rp ${subtotal.toInt()}\n" +
                        "🧾 Pajak (${taxPercent}%) + Service (${servicePercent}%) : +Rp $taxVal\n" +
                        "💰 TOTAL ESTIMASI BAYAR : Rp $grandTotal"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#0f172a"))
                setPadding(0, 12, 0, 16)
            })

            cartCard.addView(UiKit.button3D(this, "🚀 Checkout Keranjang & Buat Nomor Antrian", Color.parseColor("#059669"), Color.parseColor("#10b981")) {
                showMultiCartCheckoutDialog(subtotal, taxVal, pal)
            })
        }
        root.addView(cartCard)

        // 2. Tombol Tambah Menu/Produk Baru ke Gudang
        root.addView(UiKit.button3D(this, "➕ Tambah Menu Makanan / Produk Baru ke Katalog", Color.parseColor("#0d9488"), Color.parseColor("#2dd4bf")) {
            showAddCatalogDialog(pal)
        })

        // 3. Daftar Katalog Menu / Produk
        val icons = listOf("🍔", "☕", "🍜", "🍕", "🥤", "🧺", "🧥", "🛢️", "🔧", "💈")
        catalog.forEachIndexed { i, item ->
            val card = UiKit.card3D(this, pal.border)
            val emoji = icons[i % icons.size]
            card.addView(UiKit.pillBadge(this, item.category.uppercase(), pal.accent))
            card.addView(TextView(this).apply {
                text = "$emoji ${item.item_name}"
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
            })
            card.addView(TextView(this).apply {
                text = "💰 Rp ${item.price.toInt()} / ${item.unit}   •   📦 Stok: ${item.stock} ${item.unit}"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(pal.primary)
                setPadding(0, 8, 0, 14)
            })
            card.addView(UiKit.button3D(this, "➕ Masukkan ke Keranjang Pesanan", pal.primary, pal.accent) {
                val existing = posCart.find { it.item.id == item.id }
                if (existing != null) {
                    if (existing.qty < item.stock) existing.qty++
                } else if (item.stock > 0) {
                    posCart.add(PosCartItem(item, 1))
                }
                renderMerchantUI()
            })
            root.addView(card)
        }
    }

    // TAB PENGATURAN PAJAK BISNIS & RESTOCK GUDANG
    private fun renderStatsAndTaxTab(root: LinearLayout, pal: UiKit.Palette) {
        val taxCard = UiKit.card3D(this, pal.border)
        taxCard.addView(TextView(this).apply {
            text = "🧾 Pengaturan Pajak Bisnis & Service Charge"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(pal.primary)
        })
        taxCard.addView(TextView(this).apply {
            text = "Atur PPN / Pajak Resto (PB1) sesuai kebijakan toko Anda:"
            textSize = 13f
            setPadding(0, 4, 0, 14)
        })

        val etTax = UiKit.inputModern(this, "Pajak PPN / PB1 (%)", pal.border).apply { setText(taxPercent.toString()) }
        val etSrv = UiKit.inputModern(this, "Biaya Layanan / Service (%)", pal.border).apply { setText(servicePercent.toString()) }
        taxCard.addView(etTax)
        taxCard.addView(etSrv)

        taxCard.addView(UiKit.button3D(this, "💾 Simpan Persentase Pajak Toko Saya", pal.primary, pal.accent) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val res = api.saveMerchantSettings(mapOf(
                        "user_id" to session.getUserId().toString(),
                        "tax_percent" to etTax.text.toString(),
                        "service_percent" to etSrv.text.toString(),
                        "selected_theme" to session.getTheme()
                    ))
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, res.message ?: "Tersimpan!", Toast.LENGTH_LONG).show()
                        loadMerchantDashboard()
                    }
                } catch (e: Exception) {}
            }
        })
        root.addView(taxCard)

        // Restock Gudang
        catalog.forEach { item ->
            val stCard = UiKit.card3D(this, pal.border)
            stCard.addView(TextView(this).apply {
                text = "📦 ${item.item_name} (Stok: ${item.stock} ${item.unit})"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            })
            stCard.addView(UiKit.button3D(this, "➕ Restock Cepat +25 ${item.unit}", Color.parseColor("#059669"), Color.parseColor("#34d399")) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        api.restockItem(session.getUserId(), item.id, 25)
                        withContext(Dispatchers.Main) { loadMerchantDashboard() }
                    } catch (e: Exception) {}
                }
            })
            root.addView(stCard)
        }
    }

    private fun renderThemeTab(root: LinearLayout, pal: UiKit.Palette) {
        val card = UiKit.card3D(this, pal.border)
        card.addView(TextView(this).apply {
            text = "🎨 Pilih dari 20 Tema Warna Elegan (Tersimpan di Akun)"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        })

        UiKit.getAllThemes().forEach { (key, tPal) ->
            card.addView(UiKit.button3D(this, tPal.name, tPal.primary, tPal.accent) {
                session.setTheme(key)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        api.saveMerchantSettings(mapOf(
                            "user_id" to session.getUserId().toString(),
                            "tax_percent" to taxPercent.toString(),
                            "service_percent" to servicePercent.toString(),
                            "selected_theme" to key
                        ))
                    } catch (e: Exception) {}
                }
                renderMerchantUI()
            })
        }

        card.addView(UiKit.button3D(this, "🚪 Keluar Akun (Logout)", Color.parseColor("#e11d48"), Color.parseColor("#fb7185")) {
            session.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })
        root.addView(card)
    }

    private fun showMultiCartCheckoutDialog(subtotal: Double, taxVal: Int, pal: UiKit.Palette) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }
        val etName = UiKit.inputModern(this, "👤 Nama Pemesan / Pelanggan", pal.border)
        val etRef  = UiKit.inputModern(this, "📌 Nomor Meja / Takeaway / Catatan", pal.border)
        val etDisc = UiKit.inputModern(this, "🎁 Potongan Diskon Rp (Opsional)", pal.border).apply { setText("0") }
        box.addView(etName); box.addView(etRef); box.addView(etDisc)

        val summaryStr = posCart.joinToString(", ") { "${it.item.item_name} (x${it.qty})" }
        val cartIdsStr = posCart.joinToString(",") { "${it.item.id}:${it.qty}" }

        AlertDialog.Builder(this)
            .setTitle("🧾 Konfirmasi Pesanan & Pajak")
            .setMessage("Menu: $summaryStr\nSubtotal: Rp ${subtotal.toInt()}\nPajak/Service: +Rp $taxVal")
            .setView(box)
            .setPositiveButton("🚀 Masuk Antrian") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val res = api.createQueue(mapOf(
                            "user_id" to session.getUserId().toString(),
                            "branch_id" to branchId.toString(),
                            "prefix" to prefix,
                            "customer_name" to etName.text.toString(),
                            "customer_ref" to etRef.text.toString(),
                            "items_summary" to summaryStr,
                            "subtotal_price" to subtotal.toString(),
                            "discount_amount" to etDisc.text.toString(),
                            "cart_ids" to cartIdsStr
                        ))
                        withContext(Dispatchers.Main) {
                            posCart.clear()
                            Toast.makeText(this@MainActivity, "🎉 Antrian ${res.queue_code} dibuat! Total: Rp ${res.grand_total?.toInt()}", Toast.LENGTH_LONG).show()
                            activeTab = "kanban"
                            loadMerchantDashboard()
                        }
                    } catch (e: Exception) {}
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddCatalogDialog(pal: UiKit.Palette) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }
        val etName = UiKit.inputModern(this, "Nama Menu / Layanan", pal.border)
        val etCat  = UiKit.inputModern(this, "Kategori (Makanan / Minuman / Jasa)", pal.border)
        val etPrice = UiKit.inputModern(this, "Harga Satuan (Rp)", pal.border)
        val etStock = UiKit.inputModern(this, "Stok Awal", pal.border).apply { setText("50") }
        val etUnit  = UiKit.inputModern(this, "Satuan (porsi / cup / kg)", pal.border).apply { setText("porsi") }
        box.addView(etName); box.addView(etCat); box.addView(etPrice); box.addView(etStock); box.addView(etUnit)

        AlertDialog.Builder(this)
            .setTitle("➕ Tambah Menu / Produk Baru")
            .setView(box)
            .setPositiveButton("Simpan") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        api.addCatalogItem(session.getUserId(), etName.text.toString(), etCat.text.toString(), etPrice.text.toString(), etStock.text.toString(), etUnit.text.toString())
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

    private fun routeByRole(role: String) {
        when (role) {
            "superadmin" -> startActivity(Intent(this, SuperAdminActivity::class.java))
            "merchant"   -> startActivity(Intent(this, MerchantActivity::class.java))
            else         -> startActivity(Intent(this, CustomerActivity::class.java))
        }
        finish()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}