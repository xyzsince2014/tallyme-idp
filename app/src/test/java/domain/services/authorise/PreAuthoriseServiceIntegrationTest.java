package domain.services.authorise;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tallyme.idp.RedisClientConfig;
import tallyme.idp.WebMvcConfig;
import tallyme.idp.domain.services.authorise.PreAuthoriseService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebMvcConfig.class, RedisClientConfig.class})
@WebAppConfiguration
@ActiveProfiles("develop")
public class PreAuthoriseServiceIntegrationTest {

  @Autowired
  private PreAuthoriseService preAuthoriseService;

  // @Test
  // public void testExecute() {
  //   PreAuthoriseCache preAuthoriseCache = new PreAuthoriseCache(
  //       "AUTHORISATION_CODE",
  //       "read write delete openid profile email address phone".split(" "),
  //       "sLoBOeuIkRtEH7rXmQeCjeuc8Iz4ub1t",
  //       "http://tokyomap.live/callback",
  //       "Dxyd94mc0D4GvbC5m6pxjpqnKIwuzHn3",
  //       "7gS7IusXI3JCG8EE8C7p70yo7O7qJc8hG-Lrs6fj_0o",
  //       "SHA256",
  //       "PYoPqyBDNk00BXDQPdrpb6VZJMUBjXCC"
  //   );

  //   PreAuthoriseResponseDto actualDto = this.preAuthoriseService.execute(preAuthoriseCache);

  //   assertThat(actualDto.getClient().getClientId()).isEqualTo("sLoBOeuIkRtEH7rXmQeCjeuc8Iz4ub1t");
  //   assertThat(actualDto.getRequestId()).isNotNull();
  //   assertThat(actualDto.getRequestedScope()).isEqualTo(new String[] {"read", "write", "delete", "openid", "profile", "email", "address", "phone"});
  // }
}
