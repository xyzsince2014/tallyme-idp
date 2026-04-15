package tallyme.idp.domain.logics;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tallyme.idp.domain.entities.postgres.Usr;
import tallyme.idp.domain.repositories.postgres.UsrRepository;

@Component
public class UsrLogic {

  private final UsrRepository usrRepository;

  @Autowired
  public UsrLogic(UsrRepository usrRepository) {
    this.usrRepository = usrRepository;
  }

  /**
   * Fetches the user for a given sub.
   *
   * @param sub
   * @return
   */
  public Usr getUsrBySub(String sub) {
    Optional<Usr> optionalUsr = this.usrRepository.findById(sub);
    return optionalUsr.orElse(null);
  }

//  /**
//   * get all usrs
//   * @return usrList
//   */
//  public List<Usr> getAllUsrs() {
//    return this.usrRepository.findAll();
//  }

  /**
   * Fetches the user for the given email.
   *
   * @param email
   * @return Usr
   */
  public Usr getUsrByEmail(String email) {
    return this.usrRepository.findByEmail(email);
  }

  /**
   * Registers the given user.
   *
   * @param usr
   * @return usr registered
   */
  public Usr registerUsr(Usr usr) {
    return this.usrRepository.saveAndFlush(usr);
  }

//  /**
//   * delete the usr by sub
//   * @param sub
//   */
//  public void deleteUsrBySub(String sub) {
//    this.usrRepository.deleteById(sub);
//  }
}
