package tallyme.idp.application.authenticate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/authenticate")
public class AuthenticateController {

  @RequestMapping(method = RequestMethod.GET)
  public String preAuthenticate() {
    return "authenticate";
  }
}
