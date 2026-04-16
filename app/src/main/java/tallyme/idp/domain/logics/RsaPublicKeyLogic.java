package tallyme.idp.domain.logics;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tallyme.idp.domain.entities.postgres.RsaPublicKey;
import tallyme.idp.domain.repositories.postgres.RsaPublicKeyRepository;

@Component
public class RsaPublicKeyLogic {

  private final RsaPublicKeyRepository rsaPublicKeyRepository;

  @Autowired
  public RsaPublicKeyLogic(RsaPublicKeyRepository rsaPublicKeyRepository) {
    this.rsaPublicKeyRepository = rsaPublicKeyRepository;
  }

  /**
   * Gets the RSAPublicKey for the given kid.
   *
   * @param kid the key ID (JWK thumbprint) to look up
   * @return RSAPublicKey, or null if not found
   */
  public RSAPublicKey getRsaPublicKeyByKid(String kid) {
    Optional<RsaPublicKey> rsaPublicKeyOptional = this.rsaPublicKeyRepository.findById(kid);
    return rsaPublicKeyOptional.map(RsaPublicKey::getRsaPublicKey).orElse(null);
  }
}
