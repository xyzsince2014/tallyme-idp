package tokyomap.oauth.application.signUp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tokyomap.oauth.domain.services.signUp.SignUpException;
import tokyomap.oauth.domain.services.signUp.SignUpService;

@Controller
@RequestMapping("/sign-up")
public class SignUpController {

  private SignUpService signUpService;

  @Autowired
  public SignUpController(SignUpService signUpService) {
    this.signUpService = signUpService;
  }

  @ModelAttribute("signUpForm")
  public SignUpForm setUpForm() {
    return new SignUpForm();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String preSingUp() {
    return "signUp";
  }

  /**
   * Signs up the user.
   *
   * @param signUpForm
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded;charset=utf-8")
  public String proSingUp(@Validated SignUpForm signUpForm, BindingResult result, Model model) {
    if (result.hasErrors()) {
      return "signUp";
    }

    try {
      this.signUpService.execute(signUpForm);
      return "redirect:/authenticate"; // todo: directly sign in after signing up

    } catch (SignUpException e) {
      model.addAttribute("errorMessage", "The mail address is unavailable.");
      return "signUp";
    }
  }
}
