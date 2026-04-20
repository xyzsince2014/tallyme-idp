package tallyme.idp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import tallyme.idp.utils.Logger;

@Component
public class AuthenticationEventListeners {

  private final Logger logger;

  @Autowired
  AuthenticationEventListeners(Logger logger) {
    this.logger = logger;
  }

  @EventListener
  public void handleApplicationEvent(ApplicationEvent event) {
    this.logger.log(AuthenticationFailureBadCredentialsEvent.class.getName(), event.toString());
  }
}
