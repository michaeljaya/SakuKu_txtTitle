CREATE DATABASE IF NOT EXISTS `sakuku_db`;
USE `sakuku_db`;

CREATE TABLE IF NOT EXISTS `transaksi` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `judul` VARCHAR(100) NOT NULL,
  `nominal` DOUBLE NOT NULL,
  `tipe` ENUM('PENGELUARAN', 'PEMASUKAN') NOT NULL DEFAULT 'PENGELUARAN',
  `kategori` VARCHAR(50) NOT NULL,
  `kantong` VARCHAR(50) NOT NULL DEFAULT 'Tunai',
  `tanggal` VARCHAR(30) NOT NULL,
  `catatan` TEXT NULL,
  `is_prioritas` TINYINT(1) DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Data Dummy Awal Sesuai Desain Figcraft
INSERT INTO `transaksi` (`judul`, `nominal`, `tipe`, `kategori`, `kantong`, `tanggal`, `catatan`, `is_prioritas`) VALUES
('Warung Bu Sari', 32000, 'PENGELUARAN', 'Makan & Minum', 'Tunai', '12 Nov 2026, 12:15', 'Nasi padang makan siang kantor', 1),
('KRL Commuter Line', 6000, 'PENGELUARAN', 'Transportasi', 'GoPay', '12 Nov 2026, 08:20', 'Ongkos transport pagi', 0),
('Token Listrik PLN', 202000, 'PENGELUARAN', 'Tagihan', 'Bank BCA', '11 Nov 2026, 19:00', 'Beli token listrik rumah', 1),
('Gaji Bulanan', 8500000, 'PEMASUKAN', 'Gaji', 'Bank BCA', '10 Nov 2026, 09:00', 'Gaji bulanan masuk', 0),
('Kopi Kenangan', 22000, 'PENGELUARAN', 'Makan & Minum', 'GoPay', '10 Nov 2026, 15:30', 'Kopi break sore', 0);
