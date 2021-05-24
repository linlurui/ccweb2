package ccait.ccweb.client;


import ccait.ccweb.model.ResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;


@FeignClient(name = "ccweb-mail", path = "api/mqtt/{datasource}", fallback = CCWebMqttFallback.class)
public interface CCWebMqttClient {
    @RequestMapping( value = "publish/{table}/{topic}/{qos}/{retained}", method = RequestMethod.POST  )
    ResponseData publish(@PathVariable String datasource, @PathVariable String table,
                                @PathVariable String topic, @PathVariable Integer qos,
                                @PathVariable Integer retained, @RequestBody Map<String,String> body);


}
