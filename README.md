# 🛒 POS Store

![Android](https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-DD2C00?style=for-the-badge&logo=firebase&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

## Deskripsi

Aplikasi POS (Point of Sale) adalah aplikasi kasir berbasis Android yang digunakan untuk mengelola transaksi penjualan, produk, kategori, pegawai, dan cabang. Aplikasi ini juga dilengkapi fitur autentikasi pengguna dan pencetakan struk transaksi.

---

## Fitur Utama

### 📝 Registrasi

<img src="images/register.png" width="250">

### 🔐 Login

<img src="images/login.png" width="250">

### 🏠 Dashboard
| Admin | Kasir | Manajer |
|--------------|------------|------------|
| <img src="images/admin.png" width="220"> | <img src="images/kasir.png" width="220"> | <img src="images/manajer.png" width="220"> |

---

### 💳 Transaksi

<img src="images/login.png" width="250">

---

### 📜 Riwayat Transaksi

<img src="images/riwayat.png" width="250">

---

### 👤 Akun

<img src="images/akun.png" width="250">

---

### 📦 Manajemen Produk

| Tambah Produk | Edit Produk | Data Produk |
|---------------|-------------|-------------|
| <img src="images/tambah.png" width="220"> | <img src="images/edit.png" width="220"> | <img src="images/data.png" width="220"> |

---

### 🏷️ Manajemen Kategori

| Tambah Kategori | Edit Kategori | Data Kategori |
|-----------------|---------------|---------------|
| <img src="images/tambah1.png" width="220"> | <img src="images/edit1.png" width="220"> | <img src="images/data1.png" width="220"> |

---

### 👨‍💼 Manajemen Pegawai

| Tambah Pegawai | Edit Pegawai | Data Pegawai |
|---------------|-------------|-------------|
| <img src="images/tambah2.png" width="220"> | <img src="images/edit2.png" width="220"> | <img src="images/data2.png" width="220"> |

---

### 🏢 Manajemen Cabang

| Tambah Cabang | Edit Cabang | Data Cabang |
|--------------|------------|------------|
| <img src="images/tambah3.png" width="220"> | <img src="images/edit3.png" width="220"> | <img src="images/data3.png" width="220"> |

---

### 🖨️ Nota Printer

<img src="images/nota.png" width="250">

---

## Role Pengguna

### Admin
- Mengelola seluruh data
- Mengatur pegawai
- Mengatur cabang
- Melihat sama transaksi

### Kasir
- Melakukan transaksi
- Melihat riwayat transaksi
- Cetak nota

### Manajer
- Monitoring transaksi
- Monitoring produk
- Monitoring pegawai

---

## Teknologi yang Digunakan

| Teknologi | Kegunaan |
|-----------|----------|
| Kotlin | Bahasa Pemrograman |
| Android Studio | IDE Pengembangan |
| Firebase Authentication | Login & Register |
| Firebase Realtime Database | Penyimpanan Data |
| RecyclerView | Menampilkan Data |
| Material Design | UI/UX |

---

## 📂 Struktur Database

```text
Firebase Realtime Database
│
├── akun
│   └── userId
│       ├── foto
│       ├── uid
│       ├── nama
│       ├── email
│       └── role
│
├── produk
│   └── produkId
│       ├── createAt
│       ├── foto
│       ├── namaProduk
│       ├── idCabang
│       ├── idKategori
│       ├── cabang
│       ├── harga
│       ├── status
│       ├── kategori
│       ├── cabang
│       ├── stok
│       └── updateAt
│
├── kategori
│   └── kategoriId
│       ├── namaKategori
│       └── status
│
├── pegawai
│   └── pegawaiId
│       ├── foto
│       ├── nama
│       ├── alamat
│       └── role
│
├── cabang
│   └── cabangId
│       ├── namaCabang
│       ├── alamat
│       └── status
│
└── transaksi
    └── transaksiId
        ├── alamatCabang
        ├── cabang
        ├── item
             └── idProduk
                 ├── hargaProduk
                 ├── namaProduk
                 ├── qty
                 ├── subtotal
        ├── jam
        ├── kasir
        ├── tanggal
        ├── total
        ├── alamatCabang
        ├── pembayaran
        └── nota

```


## 🔄 Alur Sistem

```text
Register / Login
        ↓
Firebase Authentication
        ↓
Validasi Email & Password
        ↓
Ambil Data User dari Database
        ↓
Cek Role Pengguna
        ↓
Masuk ke Dashboard
        ↓
Admin / Kasir / Manajer
        ↓
Mengelola Data & Transaksi
        ↓
Data Disimpan ke Firebase
```


## 🚀 Cara Menjalankan Project

### 1. Clone Repository

```bash
git clone https://github.com/username/pos-store.git
````

---

### 2. Buka Project di Android Studio

* Buka Android Studio
* Klik **Open**
* Pilih folder project `pos-store`

---

### 3. Hubungkan Firebase

### Tambahkan File Firebase

Masukkan file:

```text
google-services.json
```

ke folder:

```text
app/
```

#### Aktifkan Firebase

* Firebase Authentication
* Firebase Realtime Database

---

### 4. Tambahkan Dependency

```gradle
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-database'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.github.bumptech.glide:glide:4.16.0'
```

---

### 5. Sync Gradle

Klik:

```text
Sync Now
```

---

### 6. Jalankan Aplikasi

Hubungkan emulator atau HP Android lalu klik:

```text
Run > app
```

atau tekan:

```text
Shift + F10
```

---

### 7. Login Aplikasi

Register akun terlebih dahulu lalu login menggunakan:

* Email
* Password

Setelah login, user akan masuk ke dashboard sesuai role masing-masing.



