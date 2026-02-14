package tokyomap.oauth.domain.services.signUp;

import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tokyomap.oauth.application.signUp.SignUpForm;
import tokyomap.oauth.domain.entities.postgres.Role;
import tokyomap.oauth.domain.entities.postgres.Usr;
import tokyomap.oauth.domain.logics.UsrLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;

@Service
public class SignUpService {

  private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();
  private static final String SCOPES = "openid profile email";

  private final UsrLogic usrLogic;

  @Autowired
  public SignUpService(UsrLogic usrLogic) {
    this.usrLogic = usrLogic;
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
    usr.setScopes(SCOPES);
    usr.setRole(Role.ROLE_USER);
    usr.setCreatedAt(now);
    usr.setUpdatedAt(now);

    // todo: usr.setPicture(signUpForm.getPicture());
    this.usrLogic.registerUsr(usr);
  }
}
