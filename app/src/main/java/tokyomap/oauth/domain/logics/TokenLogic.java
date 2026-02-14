package tokyomap.oauth.domain.logics;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tokyomap.oauth.domain.entities.postgres.AccessToken;
import tokyomap.oauth.domain.entities.postgres.RefreshToken;
import tokyomap.oauth.domain.entities.postgres.RsaPublicKey;
import tokyomap.oauth.domain.repositories.postgres.AccessTokenRepository;
import tokyomap.oauth.domain.repositories.postgres.RefreshTokenRepository;
import tokyomap.oauth.domain.repositories.postgres.RsaPublicKeyRepository;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;

@Component
public class TokenLogic {

  // in minutes
  private static final int ACCESS_TOKEN_LIFETIME = 10;
  private static final int ID_TOKEN_LIFETIME = 5;
  private static final int REFRESH_TOKEN_LIFETIME = 1440; // 1 day

  private static final String TOKEN_TYPE_BEARER = "Bearer";
  private static final String ALGORITHM = "RSA";
  private static final int KEY_SIZE = 2048;
  private static final int HOURS_JST = 9;

  private static final String TOKEN_TYPE_HINT_ACCESS_TOKEN = "access_token";
  private static final String TOKEN_TYPE_HINT_REFRESH_TOKEN = "refresh_token";

  private final AccessTokenRepository accessTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RsaPublicKeyRepository rsaPublicKeyRepository;
  private final String authServerHost;

  // todo: malfunctioning if use `private static final String[] audience = new String[] {"http://resource:8081"};`
  private final String audience; // registered resource servers

  private RSAPublicKey rsaPublicKey;
  private RSAPrivateKey rsaPrivateKey;
  private String kid;

  @Autowired
  public TokenLogic(
      AccessTokenRepository accessTokenRepository,
      RefreshTokenRepository refreshTokenRepository,
      RsaPublicKeyRepository rsaPublicKeyRepository,
      @Value("${docker.container.auth}") String containerAuth,
      @Value("${docker.container.resource}") String containerResource
  ) {
    this.accessTokenRepository = accessTokenRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.rsaPublicKeyRepository = rsaPublicKeyRepository;
    this.authServerHost = containerAuth;
    this.audience = containerResource;

    try {
      KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ALGORITHM);
      keyGenerator.initialize(KEY_SIZE);
      KeyPair kp = keyGenerator.genKeyPair();
      this.rsaPublicKey = (RSAPublicKey) kp.getPublic();
      this.rsaPrivateKey = (RSAPrivateKey) kp.getPrivate();

      // the JSON Web Key (JWK public key)
      RSAKey jwk = new RSAKey.Builder(this.rsaPublicKey).keyIDFromThumbprint().build();
      this.kid = jwk.getKeyID();

      LocalDateTime now = LocalDateTime.now();
      this.rsaPublicKeyRepository.saveAndFlush(new RsaPublicKey(this.kid, this.rsaPublicKey, now, now));

    } catch (NoSuchAlgorithmException e) {
      // todo:
    } catch (JOSEException e) {
      // todo:
    }
  }

  /**
   * Gets the AccessToken entity for the given access token.
   *
   * @param accessToken
   * @return AccessToken
   */
  public AccessToken getAccessToken(String accessToken) {
    Optional<AccessToken> optionalAccessToken = this.accessTokenRepository.findById(accessToken);
    return optionalAccessToken.orElse(null);
  }

  /**
   * Gets the RefreshToken entity for the given refresh token.
   *
   * @param refreshToken
   * @return RefreshToken
   */
  public RefreshToken getRefreshToken(String refreshToken) {
    Optional<RefreshToken> optionalRefreshToken = this.refreshTokenRepository.findById(refreshToken);
    return optionalRefreshToken.orElse(null);
  }

  /**
   * Physically deletes the token.
   *
   * @param token
   * @param tokenTypeHint
   */
  public void revokeToken(String token, String tokenTypeHint) {
    if (token == null || token.isEmpty()) {
      return;
    }

    if (tokenTypeHint.equals(TOKEN_TYPE_HINT_ACCESS_TOKEN)) {
      this.accessTokenRepository.deleteById(token);
      return;
    }
    if (tokenTypeHint.equals(TOKEN_TYPE_HINT_REFRESH_TOKEN)) {
      this.refreshTokenRepository.deleteById(token);
      return;
    }

    this.accessTokenRepository.deleteById(token);
    this.refreshTokenRepository.deleteById(token);
  }

  /**
   * Generates an access token and a refresh token for the Auth Code Flow and the Refresh Token Flow.
   * A refresh token is always issued because these flows act on behalf of a resource owner (sub is present).
   *
   * @param clientId the client that requested the tokens
   * @param sub      the resource owner's subject identifier
   * @param scopes   the granted scopes
   * @param nonce    the nonce from the original authorisation request (used in the ID token); may be null
   * @return GenerateTokensResponseDto containing access token, refresh token, and ID token
   * @throws Exception
   */
  public GenerateTokensResponseDto generateTokensWithRefreshToken(
    String clientId, String sub, String[] scopes, String nonce
  ) throws Exception {

    LocalDateTime now = LocalDateTime.now();

    String accessToken = this.createSignedJWT(sub, scopes, clientId, now, ACCESS_TOKEN_LIFETIME);
    String refreshToken = this.createSignedJWT(sub, scopes, clientId, now, REFRESH_TOKEN_LIFETIME);

    // generate an id token as well because a resource owner (sub) is always present in these flows
    String idToken = this.createIdJWT(sub, clientId, nonce, now, ID_TOKEN_LIFETIME, now);

    RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, now, now);
    RefreshToken registeredRefreshTokenEntity = this.refreshTokenRepository.saveAndFlush(refreshTokenEntity);

    AccessToken accessTokenEntity = new AccessToken(accessToken, refreshTokenEntity, now, now);
    AccessToken registeredAccessTokenEntity = this.accessTokenRepository.saveAndFlush(accessTokenEntity);

    // todo: scope must not be sent back to the client in production
    return new GenerateTokensResponseDto(
      TOKEN_TYPE_BEARER,
      registeredAccessTokenEntity.getAccessToken(),
      registeredRefreshTokenEntity.getRefreshToken(),
      idToken,
      String.join(" ", scopes)
    );
  }

  /**
   * Generates an access token only for the Client Credentials Flow.
   * No refresh token is issued because the flow has no resource owner (sub is absent),
   * and the client can re-authenticate with its own credentials at any time.
   * No ID token is issued neither.
   *
   * @param clientId the client that requested the token
   * @param scopes   the granted scopes
   * @return GenerateTokensResponseDto containing only an access token (refresh token and ID token are null)
   * @throws Exception
   */
  public GenerateTokensResponseDto generateAccessToken(String clientId, String[] scopes) throws Exception {

    LocalDateTime now = LocalDateTime.now();

    // sub is the client itself in the Client Credentials Flow (no resource owner)
    String accessToken =
      this.createSignedJWT(clientId, scopes, clientId, now, ACCESS_TOKEN_LIFETIME);

    AccessToken registeredAccessTokenEntity =
      this.accessTokenRepository.saveAndFlush(new AccessToken(accessToken, now, now));

    // todo: scope must not be sent back to the client in production
    return new GenerateTokensResponseDto(
      TOKEN_TYPE_BEARER, registeredAccessTokenEntity.getAccessToken(), String.join(" ", scopes)
    );
  }

  /**
   * Creates a JWSHeader.
   *
   * @return JWSHeader
   * @throws Exception
   */
  private JWSHeader createJWSHeader() throws Exception {

    // the JSON Web Signature Header (JWS Header)
    JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256) // the signature algorithm is the RS256
        .keyID(this.kid) // use the RsaPublicKey Thumbprint as kid
        .type(JOSEObjectType.JWT) // the type of the token
        .build();

    return jwsHeader;
  }

  /**
   * Creates a serialised signed JWT.
   *
   * @param sub      the subject (resource owner's identifier, or clientId for CCF)
   * @param scopes   the granted scopes
   * @param clientId the client the token is issued to
   * @param iat      the issued-at timestamp
   * @param minutes  the token lifetime in minutes
   * @return serialized signed JWT
   * @throws Exception
   */
  private String createSignedJWT(
    String sub, String[] scopes, String clientId, LocalDateTime iat, long minutes
  ) throws Exception {

    JWSHeader jwsHeader = this.createJWSHeader();

    // a cryptographically random value that uniquely identifies this token instance
    String jti = RandomStringUtils.random(8, true, true);

    // payload
    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
      .claim("iss", this.authServerHost) // the issuer, normally the URI of the auth server
      .claim("sub", sub != null ? sub : clientId) // the subject, normally the unique identifier for the resource owner
      .claim("aud", this.audience) // the audience, normally the URI(s) of the protected resource(s) the access token can be sent to
      .claim("iat", iat.toEpochSecond(ZoneOffset.ofHours(HOURS_JST))) // the issued-at timestamp of the token in seconds from 1 Jan 1970 (GMT)
      .claim("exp", iat.plusMinutes(minutes).toEpochSecond(ZoneOffset.ofHours(HOURS_JST))) // the expiration time
      .claim("jti", jti) // the unique identifier of the token, a value unique to each token created by the issuer
      .claim("scopes", scopes)
      .claim("clientId", clientId)
      .build();

    SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
    RSASSASigner signer = new RSASSASigner(this.rsaPrivateKey);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }

  /**
   * Creates an ID JWT serialised.
   * ID token must have aud and nonce claims besides claims of access token for security purpose.
   *
   * @param sub
   * @param clientId
   * @param nonce
   * @param iat
   * @param minutes
   * @param authTime
   * @return serialised signed JWT
   * @throws Exception
   */
  private String createIdJWT(
    String sub, String clientId, String nonce, LocalDateTime iat, long minutes, LocalDateTime authTime
  ) throws Exception {

    // payload
    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
      .claim("iss", this.authServerHost) // the issuer of the token, i.e. the URL of the IdP
      .claim("sub", sub) // the subject of the token, a stable and unique identifier for the user at the IdP, which is usually a machine-readable string and shouldn’t be used as a username.
      .claim("aud", clientId) // the audience of the id token that must contain the client ID of the RP
      .claim("iat", iat.toEpochSecond(ZoneOffset.ofHours(HOURS_JST))) // the timestamp at which the token is issued
      .claim("exp", iat.plusMinutes(minutes).toEpochSecond(ZoneOffset.ofHours(HOURS_JST))) // the expiration timestamp of the token. all ID tokens expire, usually pretty quickly.
      .claim("nonce", nonce) // a string sent by the RP during the authentication request, used to mitigate replay attacks. It must be included if the RP sends it
      .claim("authTime", authTime.toEpochSecond(ZoneOffset.ofHours(HOURS_JST))) // the timestamp at which the user authenticated to the IdP
      .claim("amr", new String[] {"pwd"}) // the authentication method reference, which indicates how the user authenticated to the IdP, e.g. pwd (by password), otp (by password and one-time password), sms (by SMS), email (by mail).
      // todo: .claim("atHash", accessToken) // cryptographic hash of the access token
      // todo: .claim("cHash", hashed authorisation code) // cryptographic hash of the authorization code
      .build();

    SignedJWT signedJWT = new SignedJWT(this.createJWSHeader(), jwtClaimsSet);
    RSASSASigner signer = new RSASSASigner(this.rsaPrivateKey);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }

  /**
   * Gets the RSAPublicKey for the given kid.
   *
   * @param kid
   * @return RSAPublicKey
   */
  public RSAPublicKey getRsaPublicKeyByKid(String kid) {
    Optional<RsaPublicKey> rsaPublicKeyOptional = this.rsaPublicKeyRepository.findById(kid);
    if (rsaPublicKeyOptional == null) {
      return null;
    }
    return rsaPublicKeyOptional.get().getRsaPublicKey();
  }
}
