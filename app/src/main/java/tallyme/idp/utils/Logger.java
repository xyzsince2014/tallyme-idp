package tallyme.idp.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class Logger {

  /**
   * logging message for development
   * @param message
   */
  public void log(String module, String message) {
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    try (FileOutputStream fos = new FileOutputStream("/usr/share/tomcat/logs/application." + sdf.format(date) + ".log", true)){
      System.setOut(new PrintStream(fos));
      SimpleDateFormat sdfForLog = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
      System.out.println(sdfForLog.format(date) + " INFO [" + module + "] " + message);
    }

    catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
