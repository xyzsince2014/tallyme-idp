package tallyme.idp.domain.services.signUp;

import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tallyme.idp.application.signUp.SignUpForm;
import tallyme.idp.domain.entities.postgres.Role;
import tallyme.idp.domain.entities.postgres.Usr;
import tallyme.idp.domain.logics.UsrLogic;

@Service
public class SignUpService {

  private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

  private final String defaultScope;
  private final UsrLogic usrLogic;

  @Autowired
  public SignUpService(
    UsrLogic usrLogic,
    @Value("${user.default-scope}") String defaultScope
  ) {
    this.usrLogic = usrLogic;
    this.defaultScope = defaultScope;
  }

  /**
   * Executes the user's sign up.
   *
   * @param signUpForm
   * @throws SignUpException
   */
  public void execute(SignUpForm signUpForm) throws SignUpException {
    // avoid duplicates
    if (this.usrLogic.getUsrByEmail(signUpForm.getEmail()) != null) {
      throw new SignUpException("Email already registered.");
    }

    LocalDateTime now = LocalDateTime.now();

    Usr usr = new Usr();
    usr.setSub(RandomStringUtils.random(32, true, true));
    usr.setEmail(signUpForm.getEmail());
    usr.setEmailVerified(false);
    usr.setName(signUpForm.getName());
    usr.setPassword(B_CRYPT_PASSWORD_ENCODER.encode(signUpForm.getPassword()));
    usr.setPhoneNumberVerified(false);
    usr.setScope(defaultScope);
    usr.setRole(Role.ROLE_USER);
    usr.setCreatedAt(now);
    usr.setUpdatedAt(now);

    // todo: usr.setPicture(signUpForm.getPicture());
    this.usrLogic.registerUsr(usr);
  }
}
