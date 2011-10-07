package com.freshbooks.model;

import java.util.ArrayList;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concentrates properties and methods common both to Invoice and Recurring
 */
public abstract class AbstractInvoiceTemplate {

  @XStreamAlias("client_id")
  Long clientId;

  Date date;

  @XStreamAlias("po_number")
  String poNumber;

  Double discount;
  String notes;
  String terms;

  @XStreamAlias("currency_code")
  String currencyCode;

  String language;

  @XStreamAlias("first_name")
  String firstName;

  @XStreamAlias("last_name")
  String lastName;

  String organization;

  @XStreamAlias("p_street1")
  String street1;
  @XStreamAlias("p_street2")
  String street2;
  @XStreamAlias("p_city")
  String city;
  @XStreamAlias("p_state")
  String state;
  @XStreamAlias("p_country")
  String country;
  @XStreamAlias("p_code")
  String code;

  ArrayList<InvoiceLine> lines;

  Double amount;

  public abstract Long getId();

  public abstract void setId(Long id);

  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getPoNumber() {
    return poNumber;
  }

  public void setPoNumber(String poNumber) {
    this.poNumber = poNumber;
  }

  public Double getDiscount() {
    return discount;
  }

  public void setDiscount(Double discount) {
    this.discount = discount;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getTerms() {
    return terms;
  }

  public void setTerms(String terms) {
    this.terms = terms;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getStreet1() {
    return street1;
  }

  public void setStreet1(String street1) {
    this.street1 = street1;
  }

  public String getStreet2() {
    return street2;
  }

  public void setStreet2(String street2) {
    this.street2 = street2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public ArrayList<InvoiceLine> getLines() {
    return lines;
  }

  public void setLines(ArrayList<InvoiceLine> lines) {
    this.lines = lines;
  }

  public Double getAmount() {
    return amount;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof AbstractInvoiceTemplate))
      return false;
    AbstractInvoiceTemplate other = (AbstractInvoiceTemplate) obj;
    if (amount == null) {
      if (other.amount != null)
        return false;
    } else if (!amount.equals(other.amount))
      return false;
    if (city == null) {
      if (other.city != null)
        return false;
    } else if (!city.equals(other.city))
      return false;
    if (clientId == null) {
      if (other.clientId != null)
        return false;
    } else if (!clientId.equals(other.clientId))
      return false;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (country == null) {
      if (other.country != null)
        return false;
    } else if (!country.equals(other.country))
      return false;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    if (discount == null) {
      if (other.discount != null)
        return false;
    } else if (!discount.equals(other.discount))
      return false;
    if (firstName == null) {
      if (other.firstName != null)
        return false;
    } else if (!firstName.equals(other.firstName))
      return false;
    if (lastName == null) {
      if (other.lastName != null)
        return false;
    } else if (!lastName.equals(other.lastName))
      return false;
    if (lines == null) {
      if (other.lines != null)
        return false;
    } else {
      if (lines.size() != other.lines.size())
        return false;
      for (int i = 0; i < lines.size(); i++) {
        if (!lines.get(i).equals(other.lines.get(i)))
          return false;
      }
    }
    if (notes == null) {
      if (other.notes != null)
        return false;
    } else if (!notes.equals(other.notes))
      return false;
    if (organization == null) {
      if (other.organization != null)
        return false;
    } else if (!organization.equals(other.organization))
      return false;
    if (poNumber == null) {
      if (other.poNumber != null)
        return false;
    } else if (!poNumber.equals(other.poNumber))
      return false;
    if (state == null) {
      if (other.state != null)
        return false;
    } else if (!state.equals(other.state))
      return false;
    if (street1 == null) {
      if (other.street1 != null)
        return false;
    } else if (!street1.equals(other.street1))
      return false;
    if (street2 == null) {
      if (other.street2 != null)
        return false;
    } else if (!street2.equals(other.street2))
      return false;
    if (terms == null) {
      if (other.terms != null)
        return false;
    } else if (!terms.equals(other.terms))
      return false;
    if (currencyCode == null) {
      if (other.currencyCode != null)
        return false;
    } else if (!currencyCode.equals(other.currencyCode))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;

    return true;
  }

}
