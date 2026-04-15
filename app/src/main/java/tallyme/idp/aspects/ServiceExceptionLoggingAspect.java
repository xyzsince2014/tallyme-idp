package tallyme.idp.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExceptionLoggingAspect {
  @AfterThrowing(value = "execution(* *..*Service.*(..))", throwing = "e")
  public void log(JoinPoint joinPoint, RuntimeException e) {
    System.out.println("A method has thrown an exception " + joinPoint.getSignature());
    e.printStackTrace();
  }
}
