package com.medidropbox.repository;

import com.medidropbox.entity.ArchivedPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivedPatientRepository extends JpaRepository<ArchivedPatient, Long> {
    
    /**
     * Find archived patient by original global patient ID
     */
    Optional<ArchivedPatient> findByOriginalGlobalPatientId(Long originalGlobalPatientId);
    
    /**
     * Find all expired archives that need permanent deletion
     */
    @Query("SELECT a FROM ArchivedPatient a WHERE a.archiveExpiresAt <= :now " +
           "AND a.permanentlyDeleted = false")
    List<ArchivedPatient> findExpiredArchives(@Param("now") LocalDateTime now);
    
    /**
     * Check if patient data is archived
     */
    boolean existsByOriginalGlobalPatientId(Long originalGlobalPatientId);
}
