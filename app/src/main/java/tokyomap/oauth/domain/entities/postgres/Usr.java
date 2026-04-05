package tokyomap.oauth.domain.entities.postgres;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

@Entity
@Table(name = "t_usr")
public class Usr implements Serializable {

  private static final long serialVersionUID = 3500988921393233705L;

  @Id
  @Column(name = "sub")
  private String sub;

  @Column(name = "name")
  private String name;

  @Column(name = "family_name")
  private String familyName;

  @Column(name = "given_name")
  private String givenName;

  @Column(name = "middle_name")
  private String middleName;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "preferred_username")
  private String preferredUsername;

  @Column(name = "profile")
  private String profile;

  @Column(name = "picture")
  private String picture;

  @Column(name = "website")
  private String website;

  @Column(name = "zoneinfo")
  private String zoneinfo;

  @Column(name = "locale")
  private String locale;

  @Column(name = "password")
  private String password;

  @Column(name = "email")
  @Email
  private String email;

  @Column(name = "email_verified")
  private Boolean emailVerified;

  @Column(name = "address")
  private String address;

  @Column(name = "phone")
  private String phone;

  @Column(name = "phone_number_verified")
  private Boolean phoneNumberVerified;

  @Column(name = "scope")
  private String scope;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Usr() {}

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getPreferredUsername() {
    return preferredUsername;
  }

  public void setPreferredUsername(String preferredUsername) {
    this.preferredUsername = preferredUsername;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getZoneinfo() {
    return zoneinfo;
  }

  public void setZoneinfo(String zoneinfo) {
    this.zoneinfo = zoneinfo;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {return email;}

  public void setEmail(String email) {this.email = email;}

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Boolean getPhoneNumberVerified() {
    return phoneNumberVerified;
  }

  public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
    this.phoneNumberVerified = phoneNumberVerified;
  }

  public String getScope() {
    return scope;
  }

  /**
   * Returns the scope string as a List for validation and logic processing.
   * Handles null or empty strings by returning an empty list to prevent NPE.
   *
   * @return a list of individual scope strings
   */
  public List<String> getScopeList() {
    if (this.scope == null || this.scope.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(this.scope.split(" "));
  }

  /**
   * Sets the scope string after removing duplicates and preserving order.
   * e.g.　"openid profile openid" becomes "openid profile".
   *
   * @param scope the space-separated scope string from the request
   */
  public void setScope(String scope) {
    if (scope == null || scope.trim().isEmpty()) {
      this.scope = null;
      return;
    }

    // split by space, remove duplicates using LinkedHashSet to preserve order, and join back.
    Set<String> uniqueScopes = new LinkedHashSet<>(Arrays.asList(scope.split(" ")));
    this.scope = String.join(" ", uniqueScopes);
  }

  public Role getRole() { return role; }

  public void setRole(Role role) { this.role = role; }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    return "sub = " + this.sub
      + ", name = " + this.name
      + ", familyName = " + this.familyName
      + ", givenName = " + this.givenName
      + ", middleName = " + this.middleName
      + ", nickname = " + this.nickname
      + ", preferredUsername = " + this.preferredUsername
      + ", profile = " + this.profile
      + ", picture = " + this.picture
      + ", website = " + this.website
      + ", zoneinfo = " + this.zoneinfo
      + ", locale = " + this.locale
      + ", email = " + this.email;
  }
}
