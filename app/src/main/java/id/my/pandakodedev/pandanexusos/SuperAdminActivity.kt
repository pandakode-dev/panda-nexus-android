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

class SuperAdminActivity : AppCompatActivity() {

    private val api = NexusApi.create()
    private lateinit var session: SessionManager
    private var usersList: List<UserData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        loadAllUsers()
    }

    private fun loadAllUsers() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = api.getAdminUsers()
                usersList = res.users
                withContext(Dispatchers.Main) { renderAdminUI() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    renderAdminUI()
                    Toast.makeText(this@SuperAdminActivity, "Gagal memuat daftar akun: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderAdminUI() {
        val pal = UiKit.getPalette(session.getTheme())
        val scroll = ScrollView(this).apply { setBackgroundColor(pal.bgSoft) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 70)
        }

        // 1. Header Banner SuperAdmin + Objek 3D
        val banner = UiKit.gradientBanner(this, pal.primary, pal.accent)
        banner.addView(UiKit.pillBadge(this, "🛡️ SUPER ADMIN MASTER CONTROL (ANDROID)", Color.parseColor("#33000000")))
        banner.addView(TextView(this).apply {
            text = "Kelola Semua Akun, 20 Tema & Pajak"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        banner.addView(TextView(this).apply {
            text = "Total Akun Terdaftar: ${usersList.size} Akun"
            textSize = 13f
            setTextColor(Color.parseColor("#f0f9ff"))
            setPadding(0, 6, 0, 12)
        })
        banner.addView(Nexus3DView(this, pal.primary, pal.accent).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300)
        })
        root.addView(banner)

        // 2. Bar Tombol Aksi Cepat SuperAdmin
        root.addView(UiKit.button3D(this, "➕ Tambah Akun Baru (Admin / Mitra / Pelanggan)", Color.parseColor("#059669"), Color.parseColor("#10b981")) {
            showEditOrCreateUserDialog(null, pal)
        })

        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topRow.addView(UiKit.button3D(this, "🏪 Buka Mode Kasir", Color.parseColor("#ea580c"), Color.parseColor("#fb923c")) {
            startActivity(Intent(this, MerchantActivity::class.java))
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { rightMargin = 10 } })

        topRow.addView(UiKit.button3D(this, "🚪 Logout", Color.parseColor("#e11d48"), Color.parseColor("#fb7185")) {
            session.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 0.8f) })
        root.addView(topRow)

        // 3. Daftar Kartu Akun (Bisa Edit Lengkap & Hapus)
        root.addView(TextView(this).apply {
            text = "👥 Daftar Seluruh Akun Sistem (${usersList.size})"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 16, 8, 16)
        })

        usersList.forEach { u ->
            val uPal = UiKit.getPalette(u.selected_theme)
            val card = UiKit.card3D(this, uPal.border)

            val roleBadge = when (u.role) {
                "superadmin" -> "🛡️ SUPER ADMIN"
                "merchant" -> "🏪 MITRA (${u.business_category.uppercase()})"
                else -> "🙋‍♂️ PELANGGAN"
            }

            card.addView(UiKit.pillBadge(this, "$roleBadge • ${u.subscription_tier.uppercase()} • ${u.account_status.uppercase()}", uPal.primary))
            card.addView(TextView(this).apply {
                text = "${u.business_name} — ${u.full_name}"
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#0f172a"))
            })
            card.addView(TextView(this).apply {
                text = "📧 ${u.email}\n🎨 Tema Aktif: ${uPal.name}\n🧾 Pajak Bisnis: ${u.tax_percent}%  |  Service: ${u.service_percent}%"
                textSize = 13f
                setPadding(0, 8, 0, 18)
            })

            val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            btnRow.addView(UiKit.button3D(this, "✏️ Edit Akun, Tema & Pajak", uPal.primary, uPal.accent) {
                showEditOrCreateUserDialog(u, pal)
            }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 1.3f).apply { rightMargin = 10 } })

            if (u.id != session.getUserId()) {
                btnRow.addView(UiKit.button3D(this, "🗑️ Hapus", Color.parseColor("#e11d48"), Color.parseColor("#fb7185")) {
                    confirmDeleteUser(u)
                }.apply { layoutParams = LinearLayout.LayoutParams(0, 120, 0.7f) })
            }
            card.addView(btnRow)
            root.addView(card)
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showEditOrCreateUserDialog(target: UserData?, pal: UiKit.Palette) {
        val scrollBox = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(46, 26, 46, 10) }

        val etName = UiKit.inputModern(this, "👤 Nama Lengkap", pal.border).apply { setText(target?.full_name ?: "") }
        val etBiz = UiKit.inputModern(this, "🏪 Nama Bisnis / Toko", pal.border).apply { setText(target?.business_name ?: "") }
        val etEmail = UiKit.inputModern(this, "📧 Email Login", pal.border).apply { setText(target?.email ?: "") }
        val etPass = UiKit.inputModern(this, if (target == null) "🔑 Password Awal" else "🔑 Isi jika ingin ganti password", pal.border)
        val etTax = UiKit.inputModern(this, "🧾 Pajak PPN/PB1 (%)", pal.border).apply { setText((target?.tax_percent ?: 10.0).toString()) }
        val etSrv = UiKit.inputModern(this, "🛎️ Service Charge (%)", pal.border).apply { setText((target?.service_percent ?: 0.0).toString()) }

        val roles = listOf("merchant", "superadmin", "customer")
        val spRole = Spinner(this).apply {
            adapter = ArrayAdapter(this@SuperAdminActivity, android.R.layout.simple_spinner_dropdown_item, roles)
            if (target != null) setSelection(roles.indexOf(target.role).coerceAtLeast(0))
        }

        val cats = listOf("fnb", "laundry", "workshop", "custom")
        val spCat = Spinner(this).apply {
            adapter = ArrayAdapter(this@SuperAdminActivity, android.R.layout.simple_spinner_dropdown_item, cats)
            if (target != null) setSelection(cats.indexOf(target.business_category).coerceAtLeast(0))
        }

        val tiers = listOf("pro", "basic")
        val spTier = Spinner(this).apply {
            adapter = ArrayAdapter(this@SuperAdminActivity, android.R.layout.simple_spinner_dropdown_item, tiers)
            if (target != null) setSelection(tiers.indexOf(target.subscription_tier).coerceAtLeast(0))
        }

        val statuses = listOf("approved", "pending", "rejected")
        val spStatus = Spinner(this).apply {
            adapter = ArrayAdapter(this@SuperAdminActivity, android.R.layout.simple_spinner_dropdown_item, statuses)
            if (target != null) setSelection(statuses.indexOf(target.account_status).coerceAtLeast(0))
        }

        val themeKeys = UiKit.getAllThemes().keys.toList()
        val themeNames = UiKit.getAllThemes().values.map { it.name }
        val spTheme = Spinner(this).apply {
            adapter = ArrayAdapter(this@SuperAdminActivity, android.R.layout.simple_spinner_dropdown_item, themeNames)
            if (target != null) setSelection(themeKeys.indexOf(target.selected_theme).coerceAtLeast(0))
        }

        box.addView(etName); box.addView(etBiz); box.addView(etEmail); box.addView(etPass)
        box.addView(TextView(this).apply { text = "🛡️ Role & Bidang Usaha:"; typeface = Typeface.DEFAULT_BOLD })
        box.addView(spRole); box.addView(spCat)
        box.addView(TextView(this).apply { text = "🎨 Pilih dari 20 Tema Warna:"; typeface = Typeface.DEFAULT_BOLD })
        box.addView(spTheme)
        box.addView(TextView(this).apply { text = "💎 Paket & Status Approval:"; typeface = Typeface.DEFAULT_BOLD })
        box.addView(spTier); box.addView(spStatus)
        box.addView(etTax); box.addView(etSrv)
        scrollBox.addView(box)

        AlertDialog.Builder(this)
            .setTitle(if (target == null) "➕ Buat Akun Baru" else "✏️ Edit Akun: ${target.full_name}")
            .setView(scrollBox)
            .setPositiveButton("💾 Simpan") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val res = api.adminSaveUser(mapOf(
                            "user_id" to (target?.id ?: 0).toString(),
                            "full_name" to etName.text.toString(),
                            "business_name" to etBiz.text.toString(),
                            "email" to etEmail.text.toString(),
                            "password" to etPass.text.toString(),
                            "role" to spRole.selectedItem.toString(),
                            "business_category" to spCat.selectedItem.toString(),
                            "subscription_tier" to spTier.selectedItem.toString(),
                            "account_status" to spStatus.selectedItem.toString(),
                            "selected_theme" to themeKeys[spTheme.selectedItemPosition],
                            "tax_percent" to etTax.text.toString(),
                            "service_percent" to etSrv.text.toString()
                        ))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SuperAdminActivity, res.message ?: "Tersimpan!", Toast.LENGTH_LONG).show()
                            loadAllUsers()
                        }
                    } catch (e: Exception) {}
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteUser(target: UserData) {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Hapus Permanen Akun?")
            .setMessage("Yakin ingin menghapus akun ${target.full_name} (${target.business_name}) beserta seluruh datanya?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val res = api.adminDeleteUser(mapOf("user_id" to target.id.toString()))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SuperAdminActivity, res.message ?: "Terhapus", Toast.LENGTH_LONG).show()
                            loadAllUsers()
                        }
                    } catch (e: Exception) {}
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}