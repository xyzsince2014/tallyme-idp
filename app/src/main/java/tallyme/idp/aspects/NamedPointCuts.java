package tallyme.idp.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NamedPointCuts {
  @Pointcut("execution(* tallyme.idp.application.*.*Controller.*(..))")
  public void anyControllerOperation() {}

  @Pointcut("execution(* tallyme.idp.domain.services.*.*Service.*(..))")
  public void anyServiceOperation() {}
}
