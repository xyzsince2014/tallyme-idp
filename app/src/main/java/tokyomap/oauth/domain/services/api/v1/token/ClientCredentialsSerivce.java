package tokyomap.oauth.domain.services.api.v1.token;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.TokenLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.GenerateTokensRequestDto;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;
import tokyomap.oauth.dtos.TokenValidationResultDto;
import tokyomap.oauth.utils.Decorder;

@Service
public class ClientCredentialsSerivce extends TokenService<CredentialsDto> {

  // todo: use global constants
  private static final String ERROR_MESSAGE_INVALID_SCOPES = "Invalid Scopes";

  private final TokenLogic tokenLogic;

  @Autowired
  public ClientCredentialsSerivce(ClientLogic clientLogic, Decorder decorder, TokenLogic tokenLogic) {
    super(clientLogic, decorder);
    this.tokenLogic = tokenLogic;
  }

  /**
   * execute validation of request to the token endpoint
   * @return TokenValidationResultDto
   */
  @Override
  public TokenValidationResultDto<CredentialsDto> execValidation(GenerateTokensRequestDto requestDto, String authorization) throws ApiException {

    CredentialsDto credentialsDto = this.validateClient(requestDto, authorization);
    String[] requestedScopes = requestDto.getScopes();

    if (!Arrays.asList(credentialsDto.getScopes()).containsAll(Arrays.asList(requestedScopes))) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_SCOPES);
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
    GenerateTokensResponseDto responseDto =
      this.tokenLogic.generateAccessToken(tokenValidationResultDto.getClientId(), tokenValidationResultDto.getPayload().getScopes());

    return responseDto;
  }
}
