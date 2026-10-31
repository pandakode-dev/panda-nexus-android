package id.my.pandakodedev.pandanexusos

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class CustomerActivity : AppCompatActivity() {

    private val api = NexusApi.create()
    private lateinit var session: SessionManager
    private var branches: List<BranchData> = emptyList()
    private var queues: List<QueueData> = emptyList()
    private var allCatalog: List<CatalogData> = emptyList()
    private var selectedSector = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        loadCustomerData()
    }

    private fun loadCustomerData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = api.getCustomerHome()
                branches = res.branches
                queues = res.queues
                allCatalog = res.catalog
                withContext(Dispatchers.Main) { renderUI() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { renderUI() }
            }
        }
    }

    private fun renderUI() {
        val pal = UiKit.getPalette(session.getTheme())
        val scroll = ScrollView(this).apply { setBackgroundColor(pal.bgSoft) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 70)
        }

        val banner = UiKit.gradientBanner(this, pal.primary, pal.accent)
        banner.addView(UiKit.pillBadge(this, "🌈 PORTAL PESAN MENU & ANTRIAN PELANGGAN", Color.parseColor("#33000000")))
        banner.addView(TextView(this).apply {
            text = "Halo, ${session.getFullName()}! 👋"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        banner.addView(Nexus3DView(this, pal.primary, pal.accent).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300)
        })

        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRow.addView(UiKit.button3D(this, "🔄 Refresh Status", Color.parseColor("#0f172a"), Color.parseColor("#334155")) {
            loadCustomerData()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 1f).apply { rightMargin = 10 } })

        topRow.addView(UiKit.button3D(this, "🚪 Logout", Color.parseColor("#e11d48"), Color.parseColor("#fb7185")) {
            session.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 115, 0.8f) })
        banner.addView(topRow)
        root.addView(banner)

        // Filter Kategori
        val filterScroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 22 }
        }
        val filterRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("all" to "🌟 Semua", "fnb" to "🍔 Cafe & Resto", "laundry" to "🧺 Laundry", "workshop" to "🏎️ Bengkel").forEach { (k, lbl) ->
            filterRow.addView(UiKit.pillBadge(this, lbl, if (selectedSector == k) pal.primary else Color.WHITE, if (selectedSector == k) Color.WHITE else Color.BLACK).apply {
                setOnClickListener { selectedSector = k; renderUI() }
            })
        }
        filterScroll.addView(filterRow)
        root.addView(filterScroll)

        // Daftar Cabang & Tombol Pesan Menu
        val filteredBranches = if (selectedSector == "all") branches else branches.filter { it.business_category == selectedSector }
        filteredBranches.forEach { b ->
            val card = UiKit.card3D(this, pal.border)
            val crowdLabel = if (b.is_open == 0) "🔒 Tutup" else if (b.active_queues < 5) "🟢 Sepi (~10m)" else "🟡 Normal (~25m)"
            card.addView(UiKit.pillBadge(this, "$crowdLabel • Pajak Toko: ${b.tax_percent}%", pal.primary))
            card.addView(TextView(this).apply {
                text = "🏪 ${b.business_name} — ${b.branch_name}"
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
            })
            card.addView(TextView(this).apply {
                text = "📍 ${b.address}\n🎟️ Antrian Aktif: ${b.active_queues}/${b.max_capacity}"
                textSize = 13f
                setPadding(0, 6, 0, 16)
            })

            if (b.is_open == 1) {
                card.addView(UiKit.button3D(this, "🍔 Pilih Menu Pesanan & Ambil Antrian", pal.primary, pal.accent) {
                    showMenuOrderDialog(b, pal)
                })
            }
            root.addView(card)
        }

        // Status Antrian Berjalan (Lengkap Rincian Pajak)
        root.addView(TextView(this).apply {
            text = "🔍 Status Antrian & Rincian Biaya Live"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 12, 8, 14)
        })

        queues.forEach { q ->
            val qCard = UiKit.card3D(this, pal.border)
            qCard.addView(TextView(this).apply {
                text = "🎟️ ${q.queue_code} — ${q.customer_name}"
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(pal.primary)
            })
            qCard.addView(UiKit.stageProgressBar(this, q.current_stage, 4, pal.primary))
            qCard.addView(TextView(this).apply {
                text = "🏪 ${q.business_name} (${q.branch_name})\n🛍️ ${q.items_summary}\n💵 Subtotal: Rp ${q.subtotal_price.toInt()} + Pajak: Rp ${q.tax_amount.toInt()}\n💰 TOTAL: Rp ${q.estimated_price.toInt()}"
                textSize = 14f
                setPadding(0, 6, 0, 12)
            })
            qCard.addView(UiKit.button3D(this, "📲 Simpan Struk ke WhatsApp", Color.parseColor("#15803d"), Color.parseColor("#22c55e")) {
                BluetoothPrinter.shareViaWhatsApp(this@CustomerActivity, q.business_name ?: "Panda Nexus", q, "Tahap ${q.current_stage + 1}")
            })
            root.addView(qCard)
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showMenuOrderDialog(branch: BranchData, pal: UiKit.Palette) {
        val storeMenu = allCatalog.filter { it.user_id == branch.user_id }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }

        val menuNames = if (storeMenu.isNotEmpty()) {
            storeMenu.map { "${it.item_name} - Rp ${it.price.toInt()}/${it.unit}" }
        } else {
            listOf("Paket Reguler - Rp 25000")
        }

        val spMenu = Spinner(this).apply {
            adapter = ArrayAdapter(this@CustomerActivity, android.R.layout.simple_spinner_dropdown_item, menuNames)
        }
        val etQty = UiKit.inputModern(this, "🔢 Jumlah Porsi / Unit", pal.border).apply { setText("1") }
        val etRef = UiKit.inputModern(this, "📌 Nomor Meja / Plat Nomor / Catatan", pal.border)

        box.addView(TextView(this).apply { text = "🍔 Pilih Menu / Produk:"; typeface = Typeface.DEFAULT_BOLD })
        box.addView(spMenu)
        box.addView(etQty)
        box.addView(etRef)

        AlertDialog.Builder(this)
            .setTitle("🛒 Pesan di ${branch.business_name}")
            .setMessage("Pajak Toko: ${branch.tax_percent}% (Otomatis dihitung saat antrian dibuat)")
            .setView(box)
            .setPositiveButton("🚀 Kirim Pesanan & Ambil Antrian") { _, _ ->
                val qty = etQty.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 1
                val chosenItem = storeMenu.getOrNull(spMenu.selectedItemPosition)
                val unitPrice = chosenItem?.price ?: 25000.0
                val subtotal = unitPrice * qty
                val summary = if (chosenItem != null) "${chosenItem.item_name} (x$qty)" else "Paket Reguler (x$qty)"
                val cartIds = if (chosenItem != null) "${chosenItem.id}:$qty" else ""

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val res = api.createQueue(mapOf(
                            "user_id" to branch.user_id.toString(),
                            "branch_id" to branch.id.toString(),
                            "prefix" to branch.business_category.take(3).uppercase(),
                            "customer_name" to session.getFullName(),
                            "customer_ref" to etRef.text.toString(),
                            "items_summary" to summary,
                            "subtotal_price" to subtotal.toString(),
                            "cart_ids" to cartIds
                        ))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CustomerActivity,
                                "🎉 Antrian Anda: ${res.queue_code}\nTotal + Pajak: Rp ${res.grand_total?.roundToInt()}",
                                Toast.LENGTH_LONG
                            ).show()
                            loadCustomerData()
                        }
                    } catch (e: Exception) {}
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}