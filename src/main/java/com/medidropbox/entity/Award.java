package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Awards for Doctor
 */
@Entity
@Table(name = "md_awards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Award extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Award name is required")
    @Column(name = "award_name", nullable = false)
    private String awardName;

    @NotBlank(message = "Awarded by is required")
    @Column(name = "awarded_by", nullable = false)
    private String awardedBy;

    @Column(name = "award_year")
    private Integer awardYear;

    @Column(name = "award_image_url")
    private String awardImageUrl;
}
