package tallyme.idp.aspects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerNormalEndLoggingAspect {

  /**
   * an AfterReturning Advice
   * @param joinPoint
   */
  @AfterReturning("NamedPointCuts.anyControllerOperation()")
  public void log(JoinPoint joinPoint) throws IOException {
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // todo: the aspect makes controllers' integration tests malfunctioning, fix
//    try (FileOutputStream fos = new FileOutputStream("/usr/share/tomcat/logs/application." + sdf.format(date) + ".log", true)){
//      System.setOut(new PrintStream(fos));
//      SimpleDateFormat sdfForLog = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
//      Object proxy = joinPoint.getThis(); // the proxy wrapping the target
//      Object target = joinPoint.getTarget(); // the advised object
//      Object[] args = joinPoint.getArgs(); // the args of the called method
//      System.out.println(sdfForLog.format(date)
//          + " INFO [controller] A method has been invoked " + joinPoint.getSignature()
//          + ", proxy = " + proxy.toString() + ", target = " + target.toString() + ", args = " + args.toString()
//      );
//
//    } catch(FileNotFoundException e) {
//      e.printStackTrace();
//      throw e;
//    }
  }
}
