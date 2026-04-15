package tallyme.idp.domain.logics;

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
import tallyme.idp.domain.entities.postgres.AccessToken;
import tallyme.idp.domain.entities.postgres.RefreshToken;
import tallyme.idp.domain.entities.postgres.RsaPublicKey;
import tallyme.idp.domain.repositories.postgres.AccessTokenRepository;
import tallyme.idp.domain.repositories.postgres.RefreshTokenRepository;
import tallyme.idp.domain.repositories.postgres.RsaPublicKeyRepository;
import tallyme.idp.dtos.GenerateTokensResponseDto;

@Component
public class TokenLogic {

  private final AccessTokenRepository accessTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  private final String domainIdp;
  private final String audience; // registered resource server
  private final RSAPrivateKey rsaPrivateKey;
  private final String kid;
  private final int accessTokenLifetime;
  private final int idTokenLifetime;
  private final int refreshTokenLifetime;
  private final String tokenTypeBearer;
  private final String algorithm;
  private final int keySize;
  private final int hoursJst;
  private final String tokenTypeHintAccessToken;
  private final String tokenTypeHintRefreshToken;

  @Autowired
  public TokenLogic(
      AccessTokenRepository accessTokenRepository,
      RefreshTokenRepository refreshTokenRepository,
      RsaPublicKeyRepository rsaPublicKeyRepository,
      @Value("${domain.idp}") String domainIdp,
      @Value("${domain.resource}") String domainResource,
      @Value("${oauth.token.lifetime.access-minutes}") int accessTokenLifetime,
      @Value("${oauth.token.lifetime.id-minutes}") int idTokenLifetime,
      @Value("${oauth.token.lifetime.refresh-minutes}") int refreshTokenLifetime,
      @Value("${oauth.token.type.bearer}") String tokenTypeBearer,
      @Value("${rsa.algorithm}") String algorithm,
      @Value("${rsa.key-size}") int keySize,
      @Value("${timezone.offset-hours}") int hoursJst,
      @Value("${oauth.token.type.hint.access-token}") String tokenTypeHintAccessToken,
      @Value("${oauth.token.type.hint.refresh-token}") String tokenTypeHintRefreshToken
  ) throws Exception {
    this.accessTokenRepository = accessTokenRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.domainIdp = domainIdp;
    this.audience = domainResource;
    this.accessTokenLifetime = accessTokenLifetime;
    this.idTokenLifetime = idTokenLifetime;
    this.refreshTokenLifetime = refreshTokenLifetime;
    this.tokenTypeBearer = tokenTypeBearer;
    this.algorithm = algorithm;
    this.keySize = keySize;
    this.hoursJst = hoursJst;
    this.tokenTypeHintAccessToken = tokenTypeHintAccessToken;
    this.tokenTypeHintRefreshToken = tokenTypeHintRefreshToken;

    // The RSA key pair is generated once at startup and shared across all threads for the lifetime of this bean.
    // This is the standard pattern for JWT signing: read-only shared state needs no synchronisation.
    // todo:
    //  In production, the pair should be rotated on a schedule (key rotation) rather than per-request,
    //  and loaded from a secure keystore (e.g. AWS KMS, HashiCorp Vault) rather than generated in memory.
    KeyPair keyPair = this.generateRsaKeyPair();
    this.rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

    RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
    RSAKey jwk = new RSAKey.Builder(rsaPublicKey).keyIDFromThumbprint().build();
    this.kid = jwk.getKeyID();

    LocalDateTime now = LocalDateTime.now();
    rsaPublicKeyRepository.saveAndFlush(new RsaPublicKey(this.kid, rsaPublicKey, now, now));
  }

  /**
   * Generates a fresh RSA key pair to sign tokens with.
   * Extracted from the constructor so that {@code rsaPrivateKey} and {@code kid} can be {@code final}.
   *
   * @return the generated KeyPair
   * @throws NoSuchAlgorithmException if the RSA algorithm is unavailable on this JVM
   */
  private KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(algorithm);
    keyGenerator.initialize(keySize);
    return keyGenerator.genKeyPair();
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
   * @param tokenTypeHint token_type_hint is optional (RFC 7009)
   */
  public void revokeToken(String token, String tokenTypeHint) {
    if (token == null || token.isEmpty()) {
      return;
    }

    if (tokenTypeHint == null) {
      this.accessTokenRepository.deleteById(token);
      this.refreshTokenRepository.deleteById(token);
    }

    if (tokenTypeHint.equals(tokenTypeHintAccessToken)) {
      this.accessTokenRepository.deleteById(token);
    }
    if (tokenTypeHint.equals(tokenTypeHintRefreshToken)) {
      this.refreshTokenRepository.deleteById(token);
    }
  }

  /**
   * Generates an access token and a refresh token for the Auth Code Flow and the Refresh Token Flow.
   * A refresh token is always issued because these flows act on behalf of a resource owner (sub is present).
   *
   * @param clientId the client that requested the tokens
   * @param sub the resource owner's subject identifier
   * @param scope the granted scope (single space-separated String)
   * @param nonce the nonce from the original authorisation request (used in the ID token); may be null
   * @return GenerateTokensResponseDto containing access token, refresh token, and ID token
   * @throws Exception
   */
  public GenerateTokensResponseDto generateTokensWithRefreshToken(
    String clientId, String sub, String scope, String nonce
  ) throws Exception {

    LocalDateTime now = LocalDateTime.now();

    String accessToken = this.createSignedJWT(sub, scope, clientId, now, accessTokenLifetime);
    String refreshToken = this.createSignedJWT(sub, scope, clientId, now, refreshTokenLifetime);

    // generate an id token as well because a resource owner (sub) is always present in these flows
    String idToken = this.createIdJWT(sub, clientId, nonce, now, idTokenLifetime, now);

    RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, now, now);
    RefreshToken registeredRefreshTokenEntity = this.refreshTokenRepository.saveAndFlush(refreshTokenEntity);

    AccessToken accessTokenEntity = new AccessToken(accessToken, refreshTokenEntity, now, now);
    AccessToken registeredAccessTokenEntity = this.accessTokenRepository.saveAndFlush(accessTokenEntity);

    // todo: scope must not be sent back to the client in production
    return new GenerateTokensResponseDto(
      tokenTypeBearer,
      registeredAccessTokenEntity.getAccessToken(),
      registeredRefreshTokenEntity.getRefreshToken(),
      idToken,
      scope
    );
  }

  /**
   * Generates an access token only for the Client Credentials Flow.
   * No refresh token is issued because the flow has no resource owner (sub is absent),
   * and the client can re-authenticate with its own credentials at any time.
   * No ID token is issued neither.
   *
   * @param clientId the client that requested the token
   * @param scope the granted scope
   * @return GenerateTokensResponseDto containing only an access token (refresh token and ID token are null)
   * @throws Exception
   */
  public GenerateTokensResponseDto generateAccessToken(String clientId, String scope) throws Exception {

    LocalDateTime now = LocalDateTime.now();

    // sub is the client itself in the Client Credentials Flow (no resource owner)
    String accessToken =
      this.createSignedJWT(clientId, scope, clientId, now, accessTokenLifetime);

    AccessToken registeredAccessTokenEntity =
      this.accessTokenRepository.saveAndFlush(new AccessToken(accessToken, now, now));

    // todo: scope must not be sent back to the client in production
    return new GenerateTokensResponseDto(
      tokenTypeBearer, registeredAccessTokenEntity.getAccessToken(), String.join(" ", scope)
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
   * @param sub the subject (resource owner's identifier, or clientId for CCF)
   * @param scope the granted scope (single space-separated String)
   * @param clientId the client the token is issued to
   * @param iat the issued-at timestamp
   * @param minutes the token lifetime in minutes
   * @return serialized signed JWT
   * @throws Exception
   */
  private String createSignedJWT(
    String sub, String scope, String clientId, LocalDateTime iat, long minutes
  ) throws Exception {

    JWSHeader jwsHeader = this.createJWSHeader();

    // a cryptographically random value that uniquely identifies this token instance
    String jti = RandomStringUtils.random(8, true, true);

    // payload
    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
      .claim("iss", this.domainIdp) // the issuer, normally the URI of the AS
      .claim("sub", sub != null ? sub : clientId) // the subject, normally the unique identifier for the resource owner
      .claim("aud", this.audience) // the audience, normally the URI(s) of the protected resource(s) the access token can be sent to
      .claim("iat", iat.toEpochSecond(ZoneOffset.ofHours(hoursJst))) // the issued-at timestamp of the token in seconds from 1 Jan 1970 (GMT)
      .claim("exp", iat.plusMinutes(minutes).toEpochSecond(ZoneOffset.ofHours(hoursJst))) // the expiration time
      .claim("jti", jti) // the unique identifier of the token, a value unique to each token created by the issuer
      .claim("scope", scope)
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
      .claim("iss", this.domainIdp) // the issuer of the token, i.e. the URL of the IdP
      .claim("sub", sub) // the subject of the token, a stable and unique identifier for the user at the IdP, which is usually a machine-readable string and shouldn’t be used as a username.
      .claim("aud", clientId) // the audience of the id token that must contain the client ID of the RP
      .claim("iat", iat.toEpochSecond(ZoneOffset.ofHours(hoursJst))) // the timestamp at which the token is issued
      .claim("exp", iat.plusMinutes(minutes).toEpochSecond(ZoneOffset.ofHours(hoursJst))) // the expiration timestamp of the token. all ID tokens expire, usually pretty quickly.
      .claim("nonce", nonce) // a string sent by the RP during the authentication request, used to mitigate replay attacks. It must be included if the RP sends it
      .claim("authTime", authTime.toEpochSecond(ZoneOffset.ofHours(hoursJst))) // the timestamp at which the user authenticated to the IdP
      .claim("amr", new String[] {"pwd"}) // the authentication method reference, which indicates how the user authenticated to the IdP, e.g. pwd (by password), otp (by password and one-time password), sms (by SMS), email (by mail).
      // todo: .claim("atHash", accessToken) // cryptographic hash of the access token
      // todo: .claim("cHash", hashed authorisation code) // cryptographic hash of the authorization code
      .build();

    SignedJWT signedJWT = new SignedJWT(this.createJWSHeader(), jwtClaimsSet);
    RSASSASigner signer = new RSASSASigner(this.rsaPrivateKey);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }
}
