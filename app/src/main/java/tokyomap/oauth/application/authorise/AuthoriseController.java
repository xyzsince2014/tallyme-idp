package tokyomap.oauth.application.authorise;

import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import tokyomap.oauth.domain.entities.postgres.Usr;
import tokyomap.oauth.domain.entities.redis.PreAuthoriseCache;
import tokyomap.oauth.domain.services.authenticate.ResourceOwnerDetails;
import tokyomap.oauth.domain.services.authorise.InvalidPreAuthoriseException;
import tokyomap.oauth.domain.services.authorise.InvalidProAuthoriseException;
import tokyomap.oauth.domain.services.authorise.PreAuthoriseService;
import tokyomap.oauth.domain.services.authorise.ProAuthoriseService;
import tokyomap.oauth.dtos.PreAuthoriseResponseDto;

@Controller
@RequestMapping("/authorise")
public class AuthoriseController {

  private final PreAuthoriseService preAuthoriseService;
  private final ProAuthoriseService proAuthoriseService;

  @Autowired
  public AuthoriseController(PreAuthoriseService preAuthoriseService, ProAuthoriseService proAuthoriseService) {
    this.preAuthoriseService = preAuthoriseService;
    this.proAuthoriseService = proAuthoriseService;
  }

  @ModelAttribute("authorisationForm")
  public AuthorisationForm authorisationForm() {
    return new AuthorisationForm();
  }

  /**
   * Validates the authorisation request, and return the authorisation page.
   *
   * @param model
   * @param queryParams
   * @return String the html's name to render
   */
  @RequestMapping(method = RequestMethod.GET)
  public String preAuthorise(Model model, @RequestParam Map<String, String> queryParams) {

    try {
      PreAuthoriseCache preAuthoriseCache = new PreAuthoriseCache(
        queryParams.get("responseType"),
        queryParams.get("scopes").split(" "),
        queryParams.get("clientId"),
        queryParams.get("redirectUri"),
        queryParams.get("state"),
        queryParams.get("codeChallenge"),
        queryParams.get("codeChallengeMethod"),
        queryParams.get("nonce")
      );

      // auth code flow requires redirect_uri
      if (preAuthoriseCache.getRedirectUri() == null || preAuthoriseCache.getRedirectUri().equals("")) {
        return "error";
      }

      PreAuthoriseResponseDto responseDto = this.preAuthoriseService.execute(preAuthoriseCache);
      model.addAttribute("dto", responseDto);
      return "authorise";

    } catch (NullPointerException e) {
      return "error";

    } catch(InvalidPreAuthoriseException e) {
      model.addAttribute("clientUri", e.getClientUri());
      return "invalidRequest";
    }
  }

  /**
   * Authorises a request from authorise.html, issues an Authorisation Code, and redirects to the callback endpoint of the RP.
   *
   * @param authorisationForm
   * @return String
   */
  @RequestMapping(method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded;charset=utf-8")
  public String proAuthorise(
      Model model,
      @Validated AuthorisationForm authorisationForm,
      @AuthenticationPrincipal ResourceOwnerDetails resourceOwnerDetails
  ) {

    try {
      Usr resourceOwner = resourceOwnerDetails.getResourceOwner();
      URI redirectUri = this.proAuthoriseService.execute(resourceOwner, authorisationForm);

      // Spring MVC intentionally responds 302, which discards the request body containing the resource owner's credentials before redirecting,
      // while 307 preserves and re-sends the request body to the redirect_uri, which would leak the credentials to the client.
      return "redirect:" + redirectUri.toString();

    } catch (InvalidProAuthoriseException e) {
      model.addAttribute("clientUri", authorisationForm.getClientUri());
      return "invalidAuthorisation";
    }
  }
}
