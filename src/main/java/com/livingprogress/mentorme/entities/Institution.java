package com.livingprogress.mentorme.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * The institution.
 */
@Getter
@Setter
@Entity
public class Institution extends AuditableEntity {
    /**
     * The institution name.
     */
    private String institutionName;

    /**
     * The parent organization.
     */
    private String parentOrganization;

    /**
     * The street address.
     */
    private String streetAddress;

    /**
     * The city.
     */
    private String city;

    /**
     * The state.
     */
    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    /**
     * The zip code.
     */
    private String zip;

    /**
     * The country.
     */
    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    /**
     * The phone.
     */
    private String phone;

    /**
     * The email.
     */
    private String email;

    /**
     * The description.
     */
    private String description;

    /**
     * The contacts.
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstitutionContact> contacts;

    /**
     * The institution status.
     */
    @Enumerated(EnumType.STRING)
    private InstitutionStatus status;

    /**
     * The logo path.
     */
    private String logoPath;

    /**
     * Represents it is a default institution or not
     */
    private boolean defaultInstitution;
}

