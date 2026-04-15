package tallyme.idp.domain.services.api.v1.introspect;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tallyme.idp.domain.entities.postgres.AccessToken;
import tallyme.idp.domain.entities.postgres.Resource;
import tallyme.idp.domain.logics.ResourceLogic;
import tallyme.idp.domain.logics.TokenLogic;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.domain.services.api.v1.TokenScrutinyService;
import tallyme.idp.dtos.CredentialsDto;
import tallyme.idp.dtos.IntrospectResponseDto;
import tallyme.idp.utils.Decorder;

@Service
public class IntrospectService {

  private final TokenScrutinyService tokenScrutinyService;
  private final Decorder decorder;
  private final ResourceLogic resourceLogic;
  private final TokenLogic tokenLogic;

  private final String errorInvalidResource;
  private final String errorNoAuthorizationHeader;
  private final String domainIdp;
  private final String domainResource;

  @Autowired
  public IntrospectService(
      TokenScrutinyService tokenScrutinyService,
      Decorder decorder,
      ResourceLogic resourceLogic,
      TokenLogic tokenLogic,
      @Value("${error.invalid-resource}") String errorInvalidResource,
      @Value("${error.no-authorization-header}") String errorNoAuthorizationHeader,
      @Value("${domain.idp}") String domainIdp,
      @Value("${domain.resource}") String domainResource
  ) {
    this.tokenScrutinyService = tokenScrutinyService;
    this.decorder = decorder;
    this.resourceLogic = resourceLogic;
    this.tokenLogic = tokenLogic;
    this.errorInvalidResource = errorInvalidResource;
    this.errorNoAuthorizationHeader = errorNoAuthorizationHeader;
    this.domainIdp = domainIdp;
    this.domainResource = domainResource;
  }

  /**
   * Executes introspection on the given access token.
   * RFC 7662: returns active, sub, scope, and aud for a valid token.
   *
   * @param token
   * @param authorization
   * @return IntrospectResponseDto
   */
  public IntrospectResponseDto execute(String token, String authorization) throws Exception {

    // fetch resourceId, resourceSecret from the Authorization header
    CredentialsDto credentialsDto = this.decorder.decodeCredentials(authorization);
    if (credentialsDto == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, errorNoAuthorizationHeader);
    }

    Resource resource = this.resourceLogic.getResourceByResourceId(credentialsDto.getId());
    if (resource == null || !resource.getResourceSecret().equals(credentialsDto.getSecret())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, errorInvalidResource);
    }

    // RFC 7662 §2.2: a token which fails signature or format checks is simply inactive — not an error response
    SignedJWT signedJWT;
    try {
      signedJWT = this.tokenScrutinyService.execute(token);
    } catch (ApiException e) {
      return new IntrospectResponseDto(false);
    }

    // RFC 7662 §2.2: return inactive if the token is not registered (already revoked or never issued)
    AccessToken accessToken = this.tokenLogic.getAccessToken(token);
    if (accessToken == null) {
      return new IntrospectResponseDto(false);
    }

    // RFC 7662 §2.2: validate claims — return active: false for any invalid claim
    if (!this.isClaimsValid(signedJWT)) {
      return new IntrospectResponseDto(false);
    }

    return this.buildIntrospectResponseDto(signedJWT);
  }

  /**
   * Validates the JWT claims relevant to introspection per RFC 7662 §2.2:
   * - iss must match this auth server
   * - aud must contain this resource server
   * - iat ≤ now ≤ exp (token must not be expired)
   *
   * @param signedJWT
   * @return true if all claims are valid, false otherwise
   */
  private boolean isClaimsValid(SignedJWT signedJWT) throws Exception {
    JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
    Date now = new Date();

    if (!this.domainIdp.equals(claims.getIssuer())) {
      return false;
    }
    if (!claims.getAudience().contains(this.domainResource)) {
      return false;
    }
    if (now.before(claims.getIssueTime()) || now.after(claims.getExpirationTime())) {
      return false;
    }
    return true;
  }

  /**
   * Extracts RFC 7662 claims from the signed JWT and builds the introspection response.
   * - sub: the subject of the token
   * - scope: space-separated list of granted scope
   * - aud: intended audience (the resource server URI)
   *
   * @param signedJWT
   * @return IntrospectResponseDto
   */
  private IntrospectResponseDto buildIntrospectResponseDto(SignedJWT signedJWT) throws Exception {
    JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
    String sub = claims.getSubject();
    String scope = claims.getStringClaim("scope");

    // aud is a list per RFC 7519 — join to a single space-separated string
    List<String> audList = claims.getAudience();
    String aud = audList != null ? String.join(" ", audList) : null;

    return new IntrospectResponseDto(true, sub, scope, aud);
  }
}
