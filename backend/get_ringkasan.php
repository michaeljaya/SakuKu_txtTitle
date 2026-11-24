<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "koneksi.php";

$sql = "SELECT 
            COALESCE(SUM(CASE WHEN tipe = 'PEMASUKAN' THEN nominal ELSE 0 END), 0) AS total_masuk,
            COALESCE(SUM(CASE WHEN tipe = 'PENGELUARAN' THEN nominal ELSE 0 END), 0) AS total_keluar
        FROM transaksi";

$res = $conn->query($sql);
$row = $res->fetch_assoc();

$masuk = (double)$row['total_masuk'];
$keluar = (double)$row['total_keluar'];
$saldo = $masuk - $keluar;

echo json_encode([
    "result" => "SUCCESS",
    "data" => [
        "total_saldo" => $saldo,
        "total_masuk" => $masuk,
        "total_keluar" => $keluar
    ]
]);
$conn->close();
?>
