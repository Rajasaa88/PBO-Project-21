-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               MySQL
-- Database:                     showroom_sportcar
-- --------------------------------------------------------

-- 1. Buat Database jika belum ada
CREATE DATABASE IF NOT EXISTS `showroom_sportcar`;
USE `showroom_sportcar`;

-- 2. Struktur Tabel Users
CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `role` varchar(20) DEFAULT 'customer',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data untuk tabel `users`
INSERT INTO `users` (`username`, `password`, `role`) VALUES
('admin', 'admin', 'admin'),
('user', 'user', 'customer');

-- --------------------------------------------------------

-- 3. Struktur Tabel Cars
CREATE TABLE IF NOT EXISTS `cars` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `brand` varchar(50) DEFAULT NULL,
  `tier` varchar(20) DEFAULT NULL,
  `model_name` varchar(100) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `stock` int(11) DEFAULT NULL,
  `image_file` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dumping data untuk tabel `cars` (DATA SULTAN LENGKAP)
INSERT INTO `cars` (`brand`, `tier`, `model_name`, `price`, `stock`, `image_file`) VALUES
('Ferrari', 'Supercar', 'Ferrari F8 Tributo', 8500000000, 3, 'f8tributo.png'),
('Ferrari', 'Hypercar', 'Ferrari SF90 Stradale', 18000000000, 2, 'sf90.png'),
('Lamborghini', 'Supercar', 'Lamborghini Huracan', 9000000000, 4, 'huracan.png'),
('Lamborghini', 'Hypercar', 'Lamborghini Aventador SVJ', 22000000000, 1, 'aventador.png'),
('Porsche', 'Supercar', 'Porsche 911 GT3 RS', 11000000000, 5, '911gt3.png'),
('Porsche', 'Hypercar', 'Porsche 918 Spyder', 25000000000, 1, '918spyder.png'),
('Mercedes', 'Supercar', 'Mercedes-AMG G63', 7500000000, 8, 'g63.png'),
('Mercedes', 'Hypercar', 'Mercedes-AMG One', 60000000000, 1, 'amgone.png'),
('Audi', 'Supercar', 'Audi R8 V10', 8000000000, 5, 'r8v10.png'),
('Koenigsegg', 'Hypercar', 'Koenigsegg Jesko', 70000000000, 1, 'jesko.png'),
('Koenigsegg', 'Hypercar', 'Koenigsegg Gemera', 65000000000, 1, 'gemera.png'),
('Bugatti', 'Hypercar', 'Bugatti Chiron', 110000000000, 1, 'chiron.png');

-- --------------------------------------------------------

-- 4. Struktur Tabel Sales (Transaksi)
CREATE TABLE IF NOT EXISTS `sales` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `car_id` int(11) DEFAULT NULL,
  `buyer_name` varchar(50) DEFAULT NULL,
  `date_time` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;