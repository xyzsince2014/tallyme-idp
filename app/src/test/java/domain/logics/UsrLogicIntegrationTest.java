package domain.logics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tallyme.idp.PostgresJpaConfig;
import tallyme.idp.WebMvcConfig;
import tallyme.idp.domain.entities.postgres.Usr;
import tallyme.idp.domain.logics.UsrLogic;

@RunWith(SpringRunner.class) // the Runner class to run the test DI container
@ContextConfiguration(classes = {WebMvcConfig.class, PostgresJpaConfig.class}) // the test DI container's configs
@WebAppConfiguration // enable injections of DI container for web app and mock objects dependant of Servlet API
@ActiveProfiles("develop")
public class UsrLogicIntegrationTest {

  @Autowired
  private UsrLogic usrLogic; // the test target Bean

  @Test
  public void testGetUsrBySub() {
    Usr actual = this.usrLogic.getUsrBySub("00u1sneigDs6Rolmu2p6");

    assertThat(actual.getName()).isEqualTo("Ken Vinson");
    assertThat(actual.getPicture()).isNull();
    assertThat(actual.getAddress()).isEqualTo("{\"streetAddress\":\"999 Patpong Street\", \"locality\": \"unknown\", \"region\": \"Central Thailand\", \"postalCode\": \"10###\", \"country\": \"Thailand\"}");
  }
}
