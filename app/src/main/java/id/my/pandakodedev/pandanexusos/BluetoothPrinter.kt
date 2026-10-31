package id.my.pandakodedev.pandanexusos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object BluetoothPrinter {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun printQueueTicket(
        activity: Activity,
        businessName: String,
        branchName: String,
        queue: QueueData,
        stageName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                    101
                )
                Toast.makeText(activity, "Izinkan akses Bluetooth lalu klik Print lagi.", Toast.LENGTH_LONG).show()
                return
            }
        }

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(activity, "Nyalakan Bluetooth HP Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(activity, "Belum ada Printer Bluetooth yang di-pair di HP.", Toast.LENGTH_LONG).show()
            return
        }

        val deviceList = pairedDevices.toList()
        val deviceNames = deviceList.map { "🖨️ ${it.name} (${it.address})" }.toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle("🖨️ Pilih Printer Thermal 58mm/80mm")
            .setItems(deviceNames) { _, which ->
                sendEscPosToDevice(activity, deviceList[which], businessName, branchName, queue, stageName)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun sendEscPosToDevice(
        context: Context,
        device: BluetoothDevice,
        businessName: String,
        branchName: String,
        queue: QueueData,
        stageName: String
    ) {
        Thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                val out: OutputStream = socket.outputStream
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val subtotal = if (queue.subtotal_price > 0) queue.subtotal_price.toInt() else queue.estimated_price.toInt()
                val tax = queue.tax_amount.toInt()
                val disc = queue.discount_amount.toInt()
                val grand = queue.estimated_price.toInt()

                val reset = byteArrayOf(0x1B, 0x40)
                val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)
                val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
                val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
                val doubleSize = byteArrayOf(0x1D, 0x21, 0x11)
                val normalSize = byteArrayOf(0x1D, 0x21, 0x00)

                out.write(reset)
                out.write(alignCenter)
                out.write(boldOn)
                out.write("$businessName\n".toByteArray())
                out.write(boldOff)
                out.write("$branchName\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                out.write("NOMOR ANTRIAN PESANAN:\n".toByteArray())
                out.write(doubleSize)
                out.write(boldOn)
                out.write("${queue.queue_code}\n".toByteArray())
                out.write(normalSize)
                out.write(boldOff)
                out.write("Status: $stageName\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())

                out.write(alignLeft)
                out.write("Waktu    : $dateStr\n".toByteArray())
                out.write("Pelanggan: ${queue.customer_name}\n".toByteArray())
                out.write("Detail   : ${queue.customer_ref}\n".toByteArray())
                out.write("Pesanan  : ${queue.items_summary}\n".toByteArray())
                out.write("--------------------------------\n".toByteArray())
                out.write("Subtotal : Rp $subtotal\n".toByteArray())
                out.write("Pajak/Srv: +Rp $tax\n".toByteArray())
                if (disc > 0) {
                    out.write("Diskon   : -Rp $disc\n".toByteArray())
                }
                out.write("--------------------------------\n".toByteArray())

                out.write(alignCenter)
                out.write(boldOn)
                out.write("TOTAL BAYAR: Rp $grand\n".toByteArray())
                out.write(boldOff)
                out.write("--------------------------------\n".toByteArray())
                out.write("Lacak live di:\n".toByteArray())
                out.write("nexus.pandakode-dev.my.id\n\n\n\n".toByteArray())

                out.flush()
                out.close()
                socket.close()

                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "✅ Struk ${queue.queue_code} berhasil dicetak!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun shareViaWhatsApp(context: Context, businessName: String, queue: QueueData, stageName: String) {
        val subtotal = if (queue.subtotal_price > 0) queue.subtotal_price.toInt() else queue.estimated_price.toInt()
        val msg = """
            🐼 *$businessName — STRUK PESANAN & ANTRIAN*
            --------------------------------
            🎟️ Nomor Antrian: *${queue.queue_code}*
            👤 Pelanggan: *${queue.customer_name}*
            📌 Detail: ${queue.customer_ref}
            🛍️ Menu/Layanan: ${queue.items_summary}
            📊 Status: *$stageName*
            --------------------------------
            💵 Subtotal: Rp $subtotal
            🧾 Pajak & Layanan: +Rp ${queue.tax_amount.toInt()}
            🎁 Diskon: -Rp ${queue.discount_amount.toInt()}
            💰 *TOTAL BAYAR: Rp ${queue.estimated_price.toInt()}*
            --------------------------------
            🔍 Pantau status live di:
            https://nexus.pandakode-dev.my.id/?track_code=${queue.queue_code}#lacakCepat
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(msg))
        }
        context.startActivity(intent)
    }
}