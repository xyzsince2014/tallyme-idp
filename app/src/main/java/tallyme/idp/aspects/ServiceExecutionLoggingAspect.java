package tallyme.idp.aspects;

import java.io.IOException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

  /**
   * a Before Advice
   * @param joinPoint
   */
  @Before("NamedPointCuts.anyServiceOperation()") // @Before(named pointcut)
  public void log(JoinPoint joinPoint) throws IOException {
// todo: comment out because of test failure, fix
//    Date date = new Date();
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//    try (FileOutputStream fos = new FileOutputStream("/usr/share/tomcat/logs/application." + sdf.format(date) + ".log", true)){
//      System.setOut(new PrintStream(fos));
//      SimpleDateFormat sdfForLog = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
//      System.out.println(sdfForLog.format(date) + " INFO [service] A method has started " + joinPoint.getSignature());
//    } catch(FileNotFoundException e) {
//      e.printStackTrace();
//      throw e;
//    }
  }
}
