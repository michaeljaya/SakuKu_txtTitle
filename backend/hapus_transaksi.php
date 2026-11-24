<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "koneksi.php";

$id = $_POST['id'] ?? 0;

if ($id <= 0) {
    echo json_encode(["result" => "FAILED", "message" => "ID tidak valid"]);
    exit;
}

$stmt = $conn->prepare("DELETE FROM transaksi WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode(["result" => "SUCCESS", "message" => "Transaksi berhasil dihapus"]);
} else {
    echo json_encode(["result" => "FAILED", "message" => "Gagal menghapus data"]);
}
$conn->close();
?>
