package com.medidropbox.repository;

import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.hospital LEFT JOIN FETCH u.globalPatient WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameAndIsActiveTrueWithHospital(@Param("username") String username);
    
    List<User> findByHospital(Hospital hospital);
    List<User> findByHospitalId(Long hospitalId);
}
