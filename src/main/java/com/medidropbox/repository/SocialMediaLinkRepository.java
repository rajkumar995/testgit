package com.medidropbox.repository;

import com.medidropbox.entity.SocialMediaLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaLinkRepository extends JpaRepository<SocialMediaLink, Long> {
}
