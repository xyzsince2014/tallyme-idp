package tallyme.idp.domain.services.api.v1.register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.dtos.ClientValidationResultDto;
import tallyme.idp.dtos.RequestClientDto;

public abstract class RegisterService {

  private final String responseTypeCode;
  protected final String[] responseTypes;
  private final String grantTypeAuthorizationCode;
  protected final String[] grantTypes;
  protected final String tokenEndpointAuthMethodDefault;
  protected final String[] tokenEndpointAuthMethods;
  protected final String registrationEndpoint;
  protected final String errorInvalidClientName;
  private final String errorInvalidClientUri;
  private final String errorInvalidRedirectUris;
  private final String errorInvalidScope;
  protected final String errorInvalidTokenEndpointAuthMethod;
  private final String errorInvalidGrantTypes;
  private final String errorInvalidResponseTypes;

  public RegisterService(
    @Value("${oauth.response.type.code}") String responseTypeCode,
    @Value("#{'${oauth.response.types}'.split(',')}") String[] responseTypes,
    @Value("${oauth.grant.type.authorization-code}") String grantTypeAuthorizationCode,
    @Value("#{'${oauth.grant.types}'.split(',')}") String[] grantTypes,
    @Value("${oauth.token-endpoint.auth.method.default}") String tokenEndpointAuthMethodDefault,
    @Value("#{'${oauth.token-endpoint.auth.methods}'.split(',')}") String[] tokenEndpointAuthMethods,
    @Value("${REGISTRATION_ENDPOINT}") String registrationEndpoint,
    @Value("${error.invalid-client-name}") String errorInvalidClientName,
    @Value("${error.invalid-client-uri}") String errorInvalidClientUri,
    @Value("${error.invalid-redirect-uris}") String errorInvalidRedirectUris,
    @Value("${error.invalid-scope}") String errorInvalidScope,
    @Value("${error.invalid-token-endpoint-auth-method}") String errorInvalidTokenEndpointAuthMethod,
    @Value("${error.invalid-grant-types}") String errorInvalidGrantTypes,
    @Value("${error.invalid-response-types}") String errorInvalidResponseTypes
  ) {
    this.responseTypeCode = responseTypeCode;
    this.responseTypes = responseTypes;
    this.grantTypeAuthorizationCode = grantTypeAuthorizationCode;
    this.grantTypes = grantTypes;
    this.tokenEndpointAuthMethodDefault = tokenEndpointAuthMethodDefault;
    this.tokenEndpointAuthMethods = tokenEndpointAuthMethods;
    this.registrationEndpoint = registrationEndpoint;
    this.errorInvalidClientName = errorInvalidClientName;
    this.errorInvalidClientUri = errorInvalidClientUri;
    this.errorInvalidRedirectUris = errorInvalidRedirectUris;
    this.errorInvalidScope = errorInvalidScope;
    this.errorInvalidTokenEndpointAuthMethod = errorInvalidTokenEndpointAuthMethod;
    this.errorInvalidGrantTypes = errorInvalidGrantTypes;
    this.errorInvalidResponseTypes = errorInvalidResponseTypes;
  }

  /**
   * Validates the given client requesting its registration.
   *
   * @param requestClientDto
   * @return ClientValidationResultDto
   * @throws ApiException
   */
  public ClientValidationResultDto execValidation(RequestClientDto requestClientDto) throws ApiException {
    this.validateClientName(requestClientDto.getClientName());
    this.validateClientUri(requestClientDto.getClientUri());
    this.validateRedirectUris(requestClientDto.getRedirectUris());
    this.validateScope(requestClientDto.getScope());
    String tokenEndpointAuthMethod = this.validateTokenEndpointAuthMethod(requestClientDto.getTokenEndpointAuthMethod());

    return this.validateAndResolveGrantAndResponseTypes(requestClientDto, tokenEndpointAuthMethod);
  }

  /**
   * Validates client_name is present and non-blank.
   * RFC 7591 §2: client_name is a human-readable identifier for the client.
   *
   * @param clientName
   * @throws ApiException if missing or blank
   */
  private void validateClientName(String clientName) throws ApiException {
    if (clientName == null || clientName.trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidClientName);
    }
  }

  /**
   * Validates client_uri is present and non-blank.
   * RFC 7591 §2: client_uri must be an absolute URI pointing to the client's homepage.
   *
   * @param clientUri
   * @throws ApiException if missing or blank
   */
  private void validateClientUri(String clientUri) throws ApiException {
    if (clientUri == null || clientUri.trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidClientUri);
    }
  }

  /**
   * Validates redirect_uris is present and non-empty.
   * RFC 7591: redirect_uri is mandatory for client registration.
   *
   * @param redirectUris
   * @throws ApiException if missing or empty
   */
  private void validateRedirectUris(String[] redirectUris) throws ApiException {
    if (redirectUris == null || redirectUris.length == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidRedirectUris);
    }
  }

  /**
   * Validates scope is present and non-empty.
   * RFC 7591 §2: scope defines the access the client is requesting.
   *
   * @param scope
   * @throws ApiException if missing or empty
   */
  private void validateScope(String[] scope) throws ApiException {
    if (scope == null || scope.length == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidScope);
    }
  }

  /**
   * Defaults token_endpoint_auth_method to client_secret_basic if not provided,
   * then validates the value against the server's supported whitelist.
   * RFC 7591 §2: default token_endpoint_auth_method to client_secret_basic if not provided.
   *
   * @param tokenEndpointAuthMethod
   * @return the resolved token_endpoint_auth_method
   * @throws ApiException if the value is not supported
   */
  private String validateTokenEndpointAuthMethod(String tokenEndpointAuthMethod) throws ApiException {
    String resolved = tokenEndpointAuthMethod == null ? tokenEndpointAuthMethodDefault : tokenEndpointAuthMethod;
    if (Arrays.stream(tokenEndpointAuthMethods).anyMatch(authMethod -> authMethod.equals(resolved))) {
      return resolved;
    }
    throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidTokenEndpointAuthMethod);
  }

  /**
   * Validates and resolves grant_types and response_types.
   * RFC 7591 §2: if neither is provided, defaults to `authorization_code + code`.
   * If only one is provided, the other is inferred. If both are provided, consistency is enforced.
   *
   * @param requestClientDto
   * @param tokenEndpointAuthMethod
   * @return ClientValidationResultDto
   * @throws ApiException if any value is not supported
   */
  protected ClientValidationResultDto validateAndResolveGrantAndResponseTypes(
    RequestClientDto requestClientDto, String tokenEndpointAuthMethod
  ) throws ApiException {

    // RFC 7591 §2: if neither is provided, default to `authorization_code` grant with `code` response type.
    if (requestClientDto.getGrantTypes() == null && requestClientDto.getResponseTypes() == null) {
      return new ClientValidationResultDto(
        new String[] {grantTypeAuthorizationCode},
        new String[] {responseTypeCode},
        tokenEndpointAuthMethod
      );
    }

    // if only response_types is given, validate it then infer grant_types:
    // `code` response type implies `authorization_code` grant; otherwise grant_types is empty.
    if (requestClientDto.getGrantTypes() == null) {
      this.validateResponseTypes(requestClientDto.getResponseTypes());
      String[] resolvedGrantTypes = Arrays.asList(requestClientDto.getResponseTypes()).contains(responseTypeCode)
          ? new String[] {grantTypeAuthorizationCode}
          : new String[] {};
      return new ClientValidationResultDto(resolvedGrantTypes, requestClientDto.getResponseTypes(), tokenEndpointAuthMethod);
    }

    // if only grant_types is given, validate it then infer response_types:
    // `authorization_code` grant implies `code` response type; otherwise response_types is empty.
    if (requestClientDto.getResponseTypes() == null) {
      this.validateGrantTypes(requestClientDto.getGrantTypes());
      String[] resolvedResponseTypes = Arrays.asList(requestClientDto.getGrantTypes()).contains(grantTypeAuthorizationCode)
          ? new String[] {responseTypeCode}
          : new String[] {};
      return new ClientValidationResultDto(requestClientDto.getGrantTypes(), resolvedResponseTypes, tokenEndpointAuthMethod);
    }

    // both are explicitly provided — validate then enforce consistency between the two.
    this.validateGrantTypes(requestClientDto.getGrantTypes());
    this.validateResponseTypes(requestClientDto.getResponseTypes());
    return this.enforceGrantResponseTypeConsistency(
      requestClientDto.getGrantTypes(), requestClientDto.getResponseTypes(), tokenEndpointAuthMethod
    );
  }

  /**
   * Enforces consistency between grant_types and response_types:
   * - `authorization_code` grant requires `code` response type, and vice versa.
   *
   * @param grantTypes
   * @param responseTypes
   * @param tokenEndpointAuthMethod
   * @return ClientValidationResultDto with consistent grant_types and response_types
   */
  private ClientValidationResultDto enforceGrantResponseTypeConsistency(
    String[] grantTypes, String[] responseTypes, String tokenEndpointAuthMethod
  ) {
    List<String> grantTypeList = new ArrayList<>(Arrays.asList(grantTypes));
    List<String> responseTypeList = new ArrayList<>(Arrays.asList(responseTypes));

    // `authorization_code` grant present but `code` missing — append `code`.
    if (grantTypeList.contains(grantTypeAuthorizationCode) && !responseTypeList.contains(responseTypeCode)) {
      responseTypeList.add(responseTypeCode);
    }

    // `code` response type present but `authorization_code` grant missing — append `authorization_code`.
    if (responseTypeList.contains(responseTypeCode) && !grantTypeList.contains(grantTypeAuthorizationCode)) {
      grantTypeList.add(grantTypeAuthorizationCode);
    }

    return new ClientValidationResultDto(
      grantTypeList.toArray(new String[0]), responseTypeList.toArray(new String[0]), tokenEndpointAuthMethod
    );
  }

  /**
   * Validates all given grant types against the server's supported whitelist.
   *
   * @param grantTypes
   * @throws ApiException if any value is not supported
   */
  protected void validateGrantTypes(String[] grantTypes) throws ApiException {
    if (!Arrays.asList(this.grantTypes).containsAll(Arrays.asList(grantTypes))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidGrantTypes);
    }
  }

  /**
   * Validates all given response types against the server's supported whitelist.
   *
   * @param responseTypes
   * @throws ApiException if any value is not supported
   */
  protected void validateResponseTypes(String[] responseTypes) throws ApiException {
    if (!Arrays.asList(this.responseTypes).containsAll(Arrays.asList(responseTypes))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidResponseTypes);
    }
  }
}
