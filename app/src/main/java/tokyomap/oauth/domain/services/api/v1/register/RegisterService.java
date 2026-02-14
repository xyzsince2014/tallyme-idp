package tokyomap.oauth.domain.services.api.v1.register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.dtos.ClientValidationResultDto;
import tokyomap.oauth.dtos.RequestClientDto;

public abstract class RegisterService {

  /* todo: use global constants */
  private static final String RESPONSE_TYPE_AUTHORISATION_CODE = "code";

  protected static final String[] RESPONSE_TYPES = new String[] {
    RESPONSE_TYPE_AUTHORISATION_CODE,
  };

  private static final String GRANT_TYPE_AUTHORISATION_CODE = "authorization_code";
  private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  protected static final String[] GRANT_TYPES = new String[] {
    GRANT_TYPE_AUTHORISATION_CODE,
    GRANT_TYPE_REFRESH_TOKEN,
    GRANT_TYPE_CLIENT_CREDENTIALS
  };

  // RPs' client credentials are given to the token endpoint by
  // none: not given
  // client_secret_basic: in Authorization header
  // client_secret_post: in POST body
  // client_secret_jwt, private_key_jwt: in jwt
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_NONE = "none";
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_BASIC = "client_secret_basic";
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_POST = "client_secret_post";
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_JWT = "client_secret_jwt";
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_PRIVATE_KEY_JWT = "private_key_jwt";

  protected static final String[] TOKEN_ENDPOINT_AUTH_METHODS = new String[] {
    TOKEN_ENDPOINT_AUTH_METHOD_NONE,
    TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_BASIC,
    TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_POST,
    TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_JWT,
    TOKEN_ENDPOINT_AUTH_METHOD_PRIVATE_KEY_JWT
  };

  // todo: use an environmental variable
  protected static final String REGISTRATION_ENDPOINT = "https://localhost/auth/api/v1/register";

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
    this.validateScopes(requestClientDto.getScopes());
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
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Client Name.");
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
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Client Uri.");
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
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Redirect Uris.");
    }
  }

  /**
   * Validates scopes is present and non-empty.
   * RFC 7591 §2: scopes define the access the client is requesting.
   *
   * @param scopes
   * @throws ApiException if missing or empty
   */
  private void validateScopes(String[] scopes) throws ApiException {
    if (scopes == null || scopes.length == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Scopes.");
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
    String resolved = tokenEndpointAuthMethod == null ? TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_BASIC : tokenEndpointAuthMethod;
    if (Arrays.stream(TOKEN_ENDPOINT_AUTH_METHODS).anyMatch(authMethod -> authMethod.equals(resolved))) {
      return resolved;
    }
    throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid tokenEndpointAuthMethod.");
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
        new String[] {GRANT_TYPE_AUTHORISATION_CODE},
        new String[] {RESPONSE_TYPE_AUTHORISATION_CODE},
        tokenEndpointAuthMethod
      );
    }

    // if only response_types is given, validate it then infer grant_types:
    // `code` response type implies `authorization_code` grant; otherwise grant_types is empty.
    if (requestClientDto.getGrantTypes() == null) {
      this.validateResponseTypes(requestClientDto.getResponseTypes());
      String[] grantTypes = Arrays.asList(requestClientDto.getResponseTypes()).contains(RESPONSE_TYPE_AUTHORISATION_CODE)
          ? new String[] {GRANT_TYPE_AUTHORISATION_CODE}
          : new String[] {};
      return new ClientValidationResultDto(grantTypes, requestClientDto.getResponseTypes(), tokenEndpointAuthMethod);
    }

    // if only grant_types is given, validate it then infer response_types:
    // `authorization_code` grant implies `code` response type; otherwise response_types is empty.
    if (requestClientDto.getResponseTypes() == null) {
      this.validateGrantTypes(requestClientDto.getGrantTypes());
      String[] responseTypes = Arrays.asList(requestClientDto.getGrantTypes()).contains(GRANT_TYPE_AUTHORISATION_CODE)
          ? new String[] {RESPONSE_TYPE_AUTHORISATION_CODE}
          : new String[] {};
      return new ClientValidationResultDto(requestClientDto.getGrantTypes(), responseTypes, tokenEndpointAuthMethod);
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
    if (grantTypeList.contains(GRANT_TYPE_AUTHORISATION_CODE) && !responseTypeList.contains(RESPONSE_TYPE_AUTHORISATION_CODE)) {
      responseTypeList.add(RESPONSE_TYPE_AUTHORISATION_CODE);
    }

    // `code` response type present but `authorization_code` grant missing — append `authorization_code`.
    if (responseTypeList.contains(RESPONSE_TYPE_AUTHORISATION_CODE) && !grantTypeList.contains(GRANT_TYPE_AUTHORISATION_CODE)) {
      grantTypeList.add(GRANT_TYPE_AUTHORISATION_CODE);
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
    if (!Arrays.asList(GRANT_TYPES).containsAll(Arrays.asList(grantTypes))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Grant Types");
    }
  }

  /**
   * Validates all given response types against the server's supported whitelist.
   *
   * @param responseTypes
   * @throws ApiException if any value is not supported
   */
  protected void validateResponseTypes(String[] responseTypes) throws ApiException {
    if (!Arrays.asList(RESPONSE_TYPES).containsAll(Arrays.asList(responseTypes))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Response Types");
    }
  }
}
