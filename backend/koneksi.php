<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "sakuku_db";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die(json_encode(["result" => "ERROR", "message" => "Koneksi database gagal: " . $conn->connect_error]));
}
?>
