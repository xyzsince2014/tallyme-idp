package tallyme.idp.application.signOut;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/sign-out")
public class SignOutController {

  @RequestMapping(path = "/pre", method = RequestMethod.GET)
  public String preAuthenticate() {
    return "signOut";
  }
}
