package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringJpaCopyRepository extends JpaRepository<Copy, Long> {

    Optional<Copy> findByIdValue(String value);

    boolean existsByBarcode(String barcode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Copy c WHERE c.id.value = :value")
    Optional<Copy> findByIdValueForUpdate(@Param("value") String value);

    @Query("SELECT c FROM Copy c WHERE c.work.id.value = :workId AND c.status = :status ORDER BY c.technicalId ASC")
    Optional<Copy> findFirstAvailableCopyByWorkId(@Param("workId") String workId, @Param("status") CopyStatus status);
}
