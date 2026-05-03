-- Insertion de publications d'exemple
INSERT INTO `publication` (`titre`, `contenu`, `type`, `auteur_id`, `image_path`) VALUES
('Comment cultiver des tomates bio ?', 'Bonjour à tous, je partage mes astuces pour obtenir de belles tomates sans pesticides cette année. Il faut privilégier le paillage et l''arrosage au pied.', 'Discussion', 1, NULL),
('Maladie sur mes pommiers', 'Besoin d''aide ! Mes feuilles de pommiers brunissent et tombent. Est-ce la tavelure ? Que puis-je faire naturellement ?', 'Question', 1, NULL),
('Marché fermier ce dimanche !', 'Nous organisons un grand marché local ce dimanche à la ferme Firmetna. Venez nombreux découvrir nos produits de saison.', 'Annonce', 1, NULL),
('Astuces pour un compost réussi', 'Le secret d''un bon compost est l''équilibre entre les matières vertes et brunes. N''oubliez pas de le retourner régulièrement !', 'Discussion', 1, NULL),
('Recherche partenaire pour élevage', 'Je suis agriculteur et je cherche à m''associer pour développer une activité d''élevage de poules pondeuses bio.', 'Annonce', 1, NULL);

-- Insertion de quelques commentaires
INSERT INTO `commentaire` (`contenu`, `auteur_id`, `publication_id`) VALUES
('Merci pour ces conseils sur les tomates !', 1, 1),
('Je pense que c''est effectivement la tavelure. Essayez la bouillie bordelaise.', 1, 2),
('Génial, j''y serai dimanche !', 1, 3);
