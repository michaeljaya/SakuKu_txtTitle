<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "koneksi.php";

$tipe = isset($_POST['tipe']) ? $_POST['tipe'] : 'SEMUA';

if ($tipe == 'SEMUA' || empty($tipe)) {
    $sql = "SELECT * FROM transaksi ORDER BY id DESC";
    $result = $conn->query($sql);
} else {
    $stmt = $conn->prepare("SELECT * FROM transaksi WHERE tipe = ? ORDER BY id DESC");
    $stmt->bind_param("s", $tipe);
    $stmt->execute();
    $result = $stmt->get_result();
}

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = [
        "id" => (int)$row['id'],
        "judul" => $row['judul'],
        "nominal" => (double)$row['nominal'],
        "tipe" => $row['tipe'],
        "kategori" => $row['kategori'],
        "kantong" => $row['kantong'],
        "tanggal" => $row['tanggal'],
        "catatan" => $row['catatan'],
        "is_prioritas" => (int)$row['is_prioritas']
    ];
}

echo json_encode(["result" => "SUCCESS", "data" => $data]);
$conn->close();
?>
