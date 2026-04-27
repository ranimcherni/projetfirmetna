CREATE DATABASE IF NOT EXISTS `firmetna_new_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `firmetna_new_db`;

CREATE TABLE IF NOT EXISTS `user` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    `nom` VARCHAR(100) NOT NULL,
    `prenom` VARCHAR(100) NOT NULL,
    `localisation` VARCHAR(255) DEFAULT NULL,
    `bio` TEXT DEFAULT NULL,
    `specialite` VARCHAR(255) DEFAULT NULL,
    `telephone` VARCHAR(30) DEFAULT NULL,
    `image` TEXT DEFAULT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'Actif',
    `statut` VARCHAR(20) NOT NULL DEFAULT 'Actif',
    `registration_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `date_inscription` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reset_code` VARCHAR(10) DEFAULT NULL,
    `reset_expiry` TIMESTAMP NULL DEFAULT NULL,
    `mfa_secret` VARCHAR(255) DEFAULT NULL,
    `mfa_enabled` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `produit` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `description` LONGTEXT DEFAULT NULL,
    `prix` DOUBLE NOT NULL,
    `type` VARCHAR(100) NOT NULL,
    `image_url` VARCHAR(500) DEFAULT NULL,
    `unite` VARCHAR(50) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `is_bio` TINYINT(1) NOT NULL DEFAULT 0,
    `badge` VARCHAR(100) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `commande` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `date_commande` DATETIME NOT NULL,
    `statut` VARCHAR(100) NOT NULL,
    `adresse_livraison` LONGTEXT DEFAULT NULL,
    `total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `commentaire` VARCHAR(255) DEFAULT NULL,
    `client_id` INT NOT NULL,
    `email` VARCHAR(255) DEFAULT NULL,
    `client` VARCHAR(255) DEFAULT NULL,
    `date` DATE DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
