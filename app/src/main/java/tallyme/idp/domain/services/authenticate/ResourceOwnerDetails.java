package tallyme.idp.domain.services.authenticate;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tallyme.idp.domain.entities.postgres.Usr;

public class ResourceOwnerDetails implements UserDetails {

  private final Usr resourceOwner;
  private Collection<? extends GrantedAuthority> authorities;

  @Autowired
  public ResourceOwnerDetails(Usr usr, Collection<? extends GrantedAuthority> authorities) {
    this.resourceOwner = usr;
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getPassword() {
    return this.resourceOwner.getPassword();
  }

  /**
   * use usr.name as the Resource Owner's username
   * @return username
   */
  @Override
  public String getUsername() {
    return this.resourceOwner.getName();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Usr getResourceOwner() {
    return this.resourceOwner;
  }

  @Override
  public String toString() {
    return "resourceOwner = " + this.resourceOwner.toString();
  }
}
