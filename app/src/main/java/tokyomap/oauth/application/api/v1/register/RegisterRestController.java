package tokyomap.oauth.application.api.v1.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.domain.services.api.v1.register.CheckRegistrationAccessTokenService;
import tokyomap.oauth.domain.services.api.v1.register.CheckRegistrationBasicAuthService;
import tokyomap.oauth.domain.services.api.v1.register.RegisterClientService;
import tokyomap.oauth.domain.services.api.v1.register.UnregisterClientService;
import tokyomap.oauth.domain.services.api.v1.register.UpdateClientService;
import tokyomap.oauth.dtos.ClientValidationResultDto;
import tokyomap.oauth.dtos.ReadClientResponseDto;
import tokyomap.oauth.dtos.RegisterClientRequestDto;
import tokyomap.oauth.dtos.RegisterClientResponseDto;
import tokyomap.oauth.dtos.ResponseClientDto;
import tokyomap.oauth.dtos.UnregisterClientRequestDto;
import tokyomap.oauth.dtos.UpdateClientRequestDto;
import tokyomap.oauth.dtos.UpdateClientResponseDto;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterRestController {

  private final RegisterClientService registerClientService;
  private final CheckRegistrationBasicAuthService checkRegistrationBasicAuthService;
  private final CheckRegistrationAccessTokenService checkRegistrationAccessTokenService;
  private final UpdateClientService updateClientService;
  private final UnregisterClientService unregisterClientService;

  @Autowired
  public RegisterRestController(
      RegisterClientService registerClientService,
      CheckRegistrationBasicAuthService checkRegistrationBasicAuthService,
      CheckRegistrationAccessTokenService checkRegistrationAccessTokenService,
      UpdateClientService updateClientService,
      UnregisterClientService unregisterClientService
  ) {
    this.registerClientService = registerClientService;
    this.checkRegistrationBasicAuthService = checkRegistrationBasicAuthService;
    this.checkRegistrationAccessTokenService = checkRegistrationAccessTokenService;
    this.updateClientService = updateClientService;
    this.unregisterClientService = unregisterClientService;
  }

  /**
   * Registers the given client.
   *
   * @param requestDto
   * @return RegisterClientResponseDto
   */
  @RequestMapping(method = RequestMethod.POST, headers = {"Accept=application/json", "Content-Type=application/json"})
  public ResponseEntity<RegisterClientResponseDto> registerClient(
    @RequestHeader("Authorization") String authorization,
    @RequestBody RegisterClientRequestDto requestDto
  ) {
    try {
      // validate the Basic Auth header
      this.checkRegistrationBasicAuthService.execute(authorization);

      // client registration
      ClientValidationResultDto resultDto = this.registerClientService.execValidation(requestDto.getClient());
      Client clientRegistered = this.registerClientService.execute(requestDto.getClient(), resultDto);
      ResponseClientDto responseClientDto = this.convertClientToResponseClientDto(clientRegistered);
      return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterClientResponseDto(responseClientDto));

    } catch (ApiException e) {
      RegisterClientResponseDto responseDto = new RegisterClientResponseDto(e.getErrorMessage());
      return ResponseEntity.status(e.getStatusCode()).body(responseDto);

    } catch (Exception e) {
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns the registered client.
   *
   * @param clientId
   * @param authorization
   * @return ReadClientResponseDto
   */
  @RequestMapping(path = "/{clientId}", method = RequestMethod.GET, headers = "Accept=application/json")
  public ResponseEntity<ReadClientResponseDto> readClient(
    @PathVariable String clientId,
    @RequestHeader("Authorization") String authorization
  ) {
    try {
      ResponseClientDto responseClientDto = this.checkAccessTokenRegistration(clientId, authorization);
      return ResponseEntity.status(HttpStatus.OK).body(new ReadClientResponseDto(responseClientDto));

    } catch (ApiException e) {
      return ResponseEntity.status(e.getStatusCode()).body(new ReadClientResponseDto(e.getErrorMessage()));

    } catch (Exception e) {
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

  /**
   * Updates the registered client.
   *
   * @param clientId
   * @param authorization
   * @param requestDto
   * @return UpdateClientResponseDto
   */
  @RequestMapping(path = "/{clientId}", method = RequestMethod.PUT, headers = {"Accept=application/json", "Content-Type=application/json"})
  public ResponseEntity<UpdateClientResponseDto> updateClient(
      @PathVariable String clientId,
      @RequestHeader("Authorization") String authorization,
      @RequestBody UpdateClientRequestDto requestDto
  ) {
    try {
      ResponseClientDto responseClientDto = this.checkAccessTokenRegistration(clientId, authorization);

      ClientValidationResultDto validationResultDto = this.updateClientService.execValidation(requestDto.getClient());
      String clientNameToUpdate = this.updateClientService.execAdditionalValidation(requestDto.getClient(), responseClientDto);
      Client clientUpdated = this.updateClientService.execute(clientNameToUpdate, validationResultDto, responseClientDto);

      ResponseClientDto updatedResponseClientDto = this.convertClientToResponseClientDto(clientUpdated);
      return ResponseEntity.status(HttpStatus.OK).body(new UpdateClientResponseDto(updatedResponseClientDto));

    } catch (ApiException e) {
      return ResponseEntity.status(e.getStatusCode()).body(new UpdateClientResponseDto(e.getErrorMessage()));

    } catch (Exception e) {
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Unregisters the client for the given clientId.
   *
   * @param clientId
   * @param authorization
   */
  @RequestMapping(path = "/{clientId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unregisterClient(
    @PathVariable String clientId,
    @RequestHeader("Authorization") String authorization,
    @RequestBody UnregisterClientRequestDto requestDto
  ) {
    try {
      this.checkAccessTokenRegistration(clientId, authorization);
      this.unregisterClientService.execute(clientId, requestDto.getAccessToken(), requestDto.getRefreshToken());
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Checks the given clientId and registration access token.
   *
   * @param clientId
   * @param authorization
   * @return ResponseClientDto
   */
  private ResponseClientDto checkAccessTokenRegistration(String clientId, String authorization) throws ApiException {
      Client clientRegistered = this.checkRegistrationAccessTokenService.execute(clientId, authorization);
      return this.convertClientToResponseClientDto(clientRegistered);
  }

  /**
   * Converts Client to ResponseClientDto.
   *
   * @param client
   * @return ResponseClientDto
   */
  private ResponseClientDto convertClientToResponseClientDto(Client client) {
    ResponseClientDto responseClientDto = new ResponseClientDto();
    responseClientDto.setClientId(client.getClientId());
    responseClientDto.setClientSecret(client.getClientSecret());
    responseClientDto.setClientName(client.getClientName());
    responseClientDto.setClientUri(client.getClientUri());
    responseClientDto.setRedirectUris(client.getRedirectUris().split(" "));
    responseClientDto.setGrantTypes(client.getGrantTypes().split(" "));
    responseClientDto.setResponseTypes(client.getResponseTypes().split(" "));
    responseClientDto.setTokenEndpointAuthMethod(client.getTokenEndpointAuthMethod());
    responseClientDto.setScopes(client.getScopes().split(" "));
    responseClientDto.setRegistrationAccessToken(client.getRegistrationAccessToken());
    responseClientDto.setRegistrationClientUri(client.getRegistrationClientUri());
    responseClientDto.setCreatedAt(client.getCreatedAt());
    responseClientDto.setExpiresAt(client.getExpiresAt());
    return responseClientDto;
  }
}
