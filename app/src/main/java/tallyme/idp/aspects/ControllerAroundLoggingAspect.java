package tallyme.idp.aspects;

import org.aspectj.lang.JoinPoint;

//@Aspect
//@Component
public class ControllerAroundLoggingAspect {

  /**
   * an Around Advice
   * @param joinPoint
   */
  // todo: the aspect makes controllers malfunctioning, fix
//  @Around("NamedPointCuts.anyControllerOperation()")
  public void log(JoinPoint joinPoint) {
//    System.out.println("Execute a Controller method " + joinPoint.getSignature());
//    try {
//      Object proxy = joinPoint.getThis(); // the proxy wrapping the target
//      Object target = joinPoint.getTarget(); // the advised object
//      Object[] args = joinPoint.getArgs(); // the args of the called method
//      System.out.println("The method has ended normally"
//          + ", proxy = " + proxy.toString()
//          + ", target = " + target.toString()
//          + ", args = " + args.toString()
//      );
//    } catch (Exception e) {
//      e.getStackTrace();
//      throw e;
//    }
  }
}
