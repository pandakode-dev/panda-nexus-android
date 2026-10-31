package com.pandakode.nexus.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pandakode.nexus.data.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QueueTrackerActivity : AppCompatActivity() {

    private val apiService = ApiClient.create()

    private lateinit var etQueueCode: EditText
    private lateinit var btnTrack: Button
    private lateinit var cardResult: LinearLayout
    private lateinit var tvResultCode: TextView
    private lateinit var tvResultName: TextView
    private lateinit var tvResultSummary: TextView
    private lateinit var tvResultPrice: TextView
    private lateinit var tvResultStage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        etQueueCode = EditText(this).apply {
            hint = "Masukkan Kode Antrian"
            textSize = 16f
        }
        rootLayout.addView(etQueueCode)

        btnTrack = Button(this).apply {
            text = "Lacak"
        }
        rootLayout.addView(btnTrack)

        cardResult = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
            setBackgroundColor(Color.parseColor("#E0F7FA"))
            visibility = View.GONE
        }

        tvResultCode = TextView(this).apply { textSize = 18f }
        tvResultName = TextView(this).apply { textSize = 16f }
        tvResultSummary = TextView(this).apply { textSize = 16f }
        tvResultPrice = TextView(this).apply { textSize = 16f }
        tvResultStage = TextView(this).apply { textSize = 16f }

        cardResult.addView(tvResultCode)
        cardResult.addView(tvResultName)
        cardResult.addView(tvResultSummary)
        cardResult.addView(tvResultPrice)
        cardResult.addView(tvResultStage)

        rootLayout.addView(cardResult)

        setContentView(rootLayout)

        btnTrack.setOnClickListener {
            val code = etQueueCode.text.toString().trim()
            if (code.isNotEmpty()) {
                fetchQueueStatus(code)
            } else {
                Toast.makeText(this, "Masukkan kode antrian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchQueueStatus(code: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mengambil data dari feed API cabang 1
                val response = apiService.getTvFeed(1)
                val found = response.queues.find { it.queue_code.equals(code, ignoreCase = true) }

                withContext(Dispatchers.Main) {
                    if (found != null) {
                        cardResult.visibility = View.VISIBLE
                        tvResultCode.text = found.queue_code
                        tvResultName.text = "👤 ${found.customer_name}"
                        tvResultSummary.text = "🛍️ ${found.items_summary}"
                        tvResultPrice.text = "Rp ${found.estimated_price}"
                        tvResultStage.text = "Status: Tahap ke-${found.current_stage + 1}"
                    } else {
                        cardResult.visibility = View.GONE
                        Toast.makeText(this@QueueTrackerActivity, "Antrian $code tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QueueTrackerActivity, "Gagal terhubung ke server: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}