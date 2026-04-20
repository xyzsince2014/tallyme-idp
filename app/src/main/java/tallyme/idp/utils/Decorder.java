package tallyme.idp.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import tallyme.idp.dtos.CredentialsDto;

@Component
public class Decorder {

  /**
   * decode a base64 encoded string
   * @param encoodedString
   * @return decoded string
   */
  public String decodeBase64String(String encoodedString) {
    byte[] buff = (new Base64()).decode(encoodedString.getBytes());
    return new String(buff);
  }

  /**
   * Decodes an authorization header to credentials.
   *
   * @param authorization
   * @return CredentialsDto
   */
  public CredentialsDto decodeCredentials(String authorization) {
    if(authorization == null) {
      return null;
    }

    String encodedClientCredentials = authorization.substring("Basic ".length());
    String decodedString = this.decodeBase64String(encodedClientCredentials);
    String[] splitString = decodedString.split(":");

    CredentialsDto clientCredentials = new CredentialsDto(splitString[0], splitString[1]);

    return clientCredentials;
  }
}
