package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringJpaCopyRepository extends JpaRepository<Copy, Long> {

    Optional<Copy> findByIdValue(String value);

    boolean existsByBarcode(String barcode);
}
