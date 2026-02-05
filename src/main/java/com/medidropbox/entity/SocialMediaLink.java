package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Social media links for Hospital
 */
@Entity
@Table(name = "md_social_media_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaLink extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Platform name is required")
    @Column(name = "platform_name", nullable = false)
    private String platformName;

    @NotBlank(message = "Profile URL is required")
    @Column(name = "profile_url", nullable = false)
    private String profileUrl;
}
