# 🐼 Panda Nexus 3D OS — Mobile Android Client

![Version](https://img.shields.io/badge/version=5.0-vibrant_3d-blue)
![Platform](https://img.shields.io/badge/platform=Android%20Native-Kotlin-orange)
![Backend](https://img.shields.io/badge/backend=PHP%20%2F%20MySQL%20%2F%20REST%20API-green)

**Panda Nexus 3D** adalah platform SaaS manajemen antrian, kasir POS, rincian pajak dinamis, dan stok multi-cabang yang terintegrasi penuh antara Web (`nexus.pandakode-dev.my.id`) dan Aplikasi Native Android.

---

## 📱 Fitur Utama Android Native (v5.0)

1. **🛡️ Panel Master SuperAdmin Native (`SuperAdminActivity.kt`)**:
   * Tambah akun baru (SuperAdmin, Mitra Toko, atau Pelanggan) langsung dari HP.
   * Edit profil, ganti role, atur paket Basic/Pro, ubah persentase pajak (%), ganti 20 tema warna akun lain, dan hapus akun permanen.
2. **🛒 Kasir POS Multi-Item + Kalkulator Pajak & Service Charge (`MerchantActivity.kt`)**:
   * Pengaturan persentase Pajak PPN/PB1 Resto dan Biaya Layanan fleksibel.
   * Keranjang belanja multi-item (`+`/`-` porsi), hitung otomatis Subtotal + Pajak - Diskon = Grand Total.
   * Cetak struk langsung ke **Printer Bluetooth Thermal 58mm/80mm** (`BluetoothPrinter.kt`) dan bagikan struk via **WhatsApp**.
   * Panggilan suara antrian otomatis menggunakan *Text-to-Speech* (TTS Bahasa Indonesia).
3. **🙋‍♂️ Portal Pelanggan & Pemesanan Mandiri (`CustomerActivity.kt`)**:
   * Radar lokasi cabang toko yang buka dan indikator keramaian live (Sepi / Normal / Ramai).
   * Pilih menu makanan/layanan toko, lihat rincian pajak secara transparan, dan ambil antrian dapur secara instan.
4. **🎨 20 Tema Warna Elegan (`UiKit.kt`)**:
   * Dukungan penuh 20 kombinasi tema cerah berbasis putih bersih (*Sky Blue, Sun Yellow, Sakura Pink, Matcha Cafe, Caramel Bistro, Royal Emerald*, dll.).
5. **✨ Kanvas Kristal 3D Interaktif (`Nexus3DView.kt`)**:
   * Animasi objek 3D native yang merespons sentuhan jari 360 derajat.

---

## 📂 Struktur Arsitektur Modular (Kotlin 100% UI)

Proyek ini dibangun tanpa file layout XML konvensional (murni diprogram menggunakan Kotlin UI modern):
```text
id.my.pandakodedev.pandanexusos/
├── data/
│   ├── ApiClient.kt             # Retrofit Interface & REST API Client
│   └── Models.kt                # Data Classes (User, Queue, Catalog, Branch)
├── ui/ & utils/
│   ├── UiKit.kt                 # Komponen 3D Card, Button Gradasi & 20 Tema
│   ├── Nexus3DView.kt           # Kanvas Animasi 3D Interaktif Native
│   ├── BluetoothPrinter.kt      # ESC/POS Bluetooth Thermal Printer & WhatsApp E-Receipt
│   ├── SessionManager.kt        # SharedPreferences Sesi Login & Tema
│   ├── MainActivity.kt          # Portal Login & Registrasi Berornamen 3D
│   ├── SuperAdminActivity.kt    # Panel Master Kontrol SuperAdmin
│   ├── MerchantActivity.kt      # Dashboard Kasir POS, Kanban & Restock Gudang
│   └── CustomerActivity.kt      # Radar Cabang, Pemesanan Menu & Pelacak Live
