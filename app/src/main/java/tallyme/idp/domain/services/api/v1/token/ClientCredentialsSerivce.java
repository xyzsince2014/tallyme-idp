package tallyme.idp.domain.services.api.v1.token;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tallyme.idp.domain.logics.ClientLogic;
import tallyme.idp.domain.logics.TokenLogic;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.dtos.CredentialsDto;
import tallyme.idp.dtos.GenerateTokensRequestDto;
import tallyme.idp.dtos.GenerateTokensResponseDto;
import tallyme.idp.dtos.TokenValidationResultDto;
import tallyme.idp.utils.Decorder;

@Service
public class ClientCredentialsSerivce extends TokenService<CredentialsDto> {

  private final String errorInvalidScopes;
  private final TokenLogic tokenLogic;

  @Autowired
  public ClientCredentialsSerivce(
    ClientLogic clientLogic,
    Decorder decorder,
    TokenLogic tokenLogic,
    @Value("${error.invalid-client-id}") String errorInvalidClientId,
    @Value("${error.no-matching-client}") String errorNoMatchingClient,
    @Value("${error.no-matching-client-secret}") String errorNoMatchingClientSecret,
    @Value("${error.invalid-scopes}") String errorInvalidScopes
  ) {
    super(clientLogic, decorder, errorInvalidClientId, errorNoMatchingClient, errorNoMatchingClientSecret);
    this.tokenLogic = tokenLogic;
    this.errorInvalidScopes = errorInvalidScopes;
  }

  /**
   * Executes validation of request to the token endpoint.
   *
   * @return TokenValidationResultDto
   */
  @Override
  public TokenValidationResultDto<CredentialsDto> execValidation(
    GenerateTokensRequestDto requestDto, String authorization
  ) throws ApiException {

    CredentialsDto credentialsDto = this.validateClient(requestDto, authorization);
    if (!credentialsDto.getScopeList().containsAll(Arrays.asList(requestDto.getScope().split(" ")))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidScopes);
    }

    return new TokenValidationResultDto(credentialsDto.getId(), credentialsDto);
  }

  /**
   * Generates tokens.
   *
   * @param tokenValidationResultDto
   * @return GenerateTokensResponseDto
   */
  @Override
  @Transactional
  public GenerateTokensResponseDto execute(
    TokenValidationResultDto<CredentialsDto> tokenValidationResultDto
  ) throws Exception {

    // the Client Credentials Flow has no resource owner — no refresh token or ID token is issued
    GenerateTokensResponseDto responseDto = this.tokenLogic.generateAccessToken(
      tokenValidationResultDto.getClientId(),
      tokenValidationResultDto.getPayload().getScope()
    );

    return responseDto;
  }
}
