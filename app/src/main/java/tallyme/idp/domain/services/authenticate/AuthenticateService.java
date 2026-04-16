package tallyme.idp.domain.services.authenticate;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tallyme.idp.domain.entities.postgres.Role;
import tallyme.idp.domain.entities.postgres.Usr;
import tallyme.idp.domain.logics.UsrLogic;

@Service
public class AuthenticateService implements UserDetailsService {

  private final UsrLogic usrLogic;

  @Autowired
  public AuthenticateService(UsrLogic usrLogic) {
    this.usrLogic = usrLogic;
  }

  /**
   * load usr by email
   * @param email
   * @return ResourceOwnerDetails
   * @throws UsernameNotFoundException
   */
  @Override
  public ResourceOwnerDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Usr usr = this.usrLogic.getUsrByEmail(email);
    if(usr == null) {
      throw new UsernameNotFoundException("Resource Owner Not Found");
    }
    return new ResourceOwnerDetails(usr, getAuthorities(usr));
  }

  private Collection<GrantedAuthority> getAuthorities(Usr usr) {
    if(usr.getRole() == Role.ROLE_ADMIN) {
      return AuthorityUtils.createAuthorityList(Role.ROLE_ADMIN.name(), Role.ROLE_USER.name());
    }
    return AuthorityUtils.createAuthorityList(Role.ROLE_USER.name());
  }
}
