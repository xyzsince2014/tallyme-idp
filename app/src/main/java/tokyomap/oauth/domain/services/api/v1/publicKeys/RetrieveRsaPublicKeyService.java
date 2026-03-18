package tokyomap.oauth.domain.services.api.v1.publicKeys;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tokyomap.oauth.domain.logics.RsaPublicKeyLogic;

@Service
public class RetrieveRsaPublicKeyService {

  private static final String FORMAT_RSA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----%n";

  private final RsaPublicKeyLogic rsaPublicKeyLogic;
  private final Encoder encoder;

  @Autowired
  public RetrieveRsaPublicKeyService(RsaPublicKeyLogic rsaPublicKeyLogic) {
    this.rsaPublicKeyLogic = rsaPublicKeyLogic;
    this.encoder = Base64.getEncoder();
  }

  /**
   * Gets the RSAPublicKey for the given kid.
   *
   * @param kid
   * @return the PEM encoded public key
   */
  public String execute(String kid) throws Exception {
    RSAPublicKey rsaPublicKey = this.rsaPublicKeyLogic.getRsaPublicKeyByKid(kid);
    byte[] encoded = encoder.encode(rsaPublicKey.getEncoded());

    int index = 0;
    StringBuilder sb = new StringBuilder(encoded.length + 20);
    while (index < encoded.length) {
      int len = Math.min(64, encoded.length - index);
      if (index > 0) {
        sb.append("\n");
      }
      sb.append(new String(encoded, index, len, Charsets.UTF_8));
      index += len;
    }

    return String.format(FORMAT_RSA_PUBLIC_KEY, sb);
  }
}
