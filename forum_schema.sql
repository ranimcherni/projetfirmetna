-- Table pour les publications
CREATE TABLE IF NOT EXISTS `publication` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `titre` VARCHAR(255) NOT NULL,
    `contenu` TEXT NOT NULL,
    `type` VARCHAR(50) DEFAULT 'Discussion',
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `auteur_id` INT NOT NULL,
    `image_path` VARCHAR(255),
    FOREIGN KEY (`auteur_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- Table pour les commentaires
CREATE TABLE IF NOT EXISTS `commentaire` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `contenu` TEXT NOT NULL,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `auteur_id` INT NOT NULL,
    `publication_id` INT NOT NULL,
    `parent_id` INT DEFAULT NULL,
    FOREIGN KEY (`auteur_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `commentaire`(`id`) ON DELETE CASCADE
);
