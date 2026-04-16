package tallyme.idp.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JsonMapper {

  /**
   * convert a JSON String to a Map
   * @param json json文字列
   * @return json文字列を読み込んだMapオブジェクト。失敗した場合はnull
   */
  public Map<String, Object> convertJsonStringToMap(String json) {
    Map<String, Object> map = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
    } catch (Exception e) {
      e.printStackTrace();
    }
    return map;
  }

}
