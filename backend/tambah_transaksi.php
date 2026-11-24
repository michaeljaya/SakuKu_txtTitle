<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "koneksi.php";

$judul = $_POST['judul'] ?? '';
$nominal = $_POST['nominal'] ?? 0;
$tipe = $_POST['tipe'] ?? 'PENGELUARAN';
$kategori = $_POST['kategori'] ?? 'Lainnya';
$kantong = $_POST['kantong'] ?? 'Tunai';
$tanggal = $_POST['tanggal'] ?? date('d M Y, H:i');
$catatan = $_POST['catatan'] ?? '';
$is_prioritas = isset($_POST['is_prioritas']) ? (int)$_POST['is_prioritas'] : 0;

if (empty($judul) || $nominal <= 0) {
    echo json_encode(["result" => "FAILED", "message" => "Judul dan nominal wajib diisi!"]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO transaksi (judul, nominal, tipe, kategori, kantong, tanggal, catatan, is_prioritas) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("sdsssssi", $judul, $nominal, $tipe, $kategori, $kantong, $tanggal, $catatan, $is_prioritas);

if ($stmt->execute()) {
    echo json_encode(["result" => "SUCCESS", "message" => "Transaksi berhasil dicatat"]);
} else {
    echo json_encode(["result" => "FAILED", "message" => "Gagal menyimpan data ke database"]);
}
$conn->close();
?>
