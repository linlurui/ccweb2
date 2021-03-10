/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.config;

import ccait.ccweb.mqtt.CcwebMqttClient;
import entity.query.core.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MQTTSubscribeConfig {

    private static final Logger log = LoggerFactory.getLogger(MQTTSubscribeConfig.class);

    @Autowired
    private CcwebMqttClient client;

    @PostConstruct
    private void init() throws IOException {
        List topicList = ApplicationConfig.getInstance().getList("mqtt.client.subscribe.topic-list");

        Integer qos = 0;
        for(Object item : topicList) {
            try {
                if (item instanceof String) {
                    client.subscribe(item.toString(), qos);
                    continue;
                }

                Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) item;
                for (String key : map.keySet()) {
                    if (map.get(key).containsKey("qos") && map.get(key).get("qos") != null) {
                        qos = (Integer) map.get(key).get("qos");
                    }
                    client.subscribe(key, qos);
                }
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
