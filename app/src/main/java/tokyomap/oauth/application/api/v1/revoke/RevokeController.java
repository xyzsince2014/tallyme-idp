package tokyomap.oauth.application.api.v1.revoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.domain.services.api.v1.revoke.RevokeService;
import tokyomap.oauth.dtos.RevokeRequestDto;

@RestController
@RequestMapping("/api/v1/revoke")
public class RevokeController {

  private final RevokeService revokeService;

  @Autowired
  public RevokeController(RevokeService revokeService) {
    this.revokeService = revokeService;
  }

  /**
   * Revokes the tokens.
   *
   * @param token
   * @param tokenTypeHint
   * @param authorization
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded;charset=utf-8")
  public ResponseEntity revoke(
    @RequestParam("token") String token,
    @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
    @RequestHeader("Authorization") String authorization
  ) {
    RevokeRequestDto requestDto = new RevokeRequestDto();
    requestDto.setToken(token);
    requestDto.setTokenTypeHint(tokenTypeHint);

    try {
      this.revokeService.execute(requestDto, authorization);
      return new ResponseEntity(HttpStatus.NO_CONTENT);

    } catch (ApiException e) {
      // invalid client
      return ResponseEntity.status(e.getStatusCode()).body(e.getErrorMessage());

    } catch (Exception e) {
      // RFC 7009: return 204 even if internal server error occurs
      // todo: log.error("Revocation failed due to unexpected error", e);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}
