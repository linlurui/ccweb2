package ccait.ccweb.client;

import ccait.ccweb.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CCWebMqttFallback implements CCWebMqttClient {

    private ResponseData responseData = null;

    @Autowired
    void init() {
        responseData = new ResponseData();
        responseData.setMessage("network error");
        responseData.setStatus(400);
    }

    @Override
    public ResponseData publish(String datasource, String table, String topic, Integer qos, Integer retained, Map<String, String> body) {
        return null;
    }
}