package com.livingprogress.mentorme.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * The entity persisting localization data of a personal interest.
 */
@Entity
@Setter
@Getter
public class PersonalInterestLocale extends LocaleEntity {

  @ManyToOne
  private PersonalInterest personalInterest;
}
