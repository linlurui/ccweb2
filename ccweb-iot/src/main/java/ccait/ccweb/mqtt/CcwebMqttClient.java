/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.mqtt;

import entity.query.core.ApplicationConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * 创建一个MQTT客户端
 * @author linlurui
 * @date 2018-08-22
 */
@Component
public class CcwebMqttClient {

    @Value("${mqtt.client.username:}")
    private String username = "";

    @Value("${mqtt.client.password:}")
    private String password = "";

    @Value("${mqtt.client.host:127.0.0.1}")
    private String host = "127.0.0.1";

    @Value("${mqtt.client.clientId:clientId}")
    private String clientId = "clientId";

    @Value("${mqtt.client.enableClientRandom:false}")
    private Boolean enableClientRandom = false;

    @Value("${mqtt.client.timeout:10000}")
    private int completionTimeout = 10000;   //连接超时

    @Value("${mqtt.client.keepalive:10000}")
    private int keepalive = 10000;   //连接超时

    @Autowired
    private CcwebMqttCallback callback;

    private static final Logger log = LoggerFactory.getLogger(CcwebMqttClient.class);

    private MqttClient client;

    @PostConstruct
    private void init() {
        log.info("Connect MQTT Init: " + this);
        username = ApplicationConfig.getInstance().get("${mqtt.client.username}", username);
        password = ApplicationConfig.getInstance().get("${mqtt.client.password}", password);
        host = ApplicationConfig.getInstance().get("${mqtt.client.host}", host);
        clientId = ApplicationConfig.getInstance().get("${mqtt.client.clientId}", clientId);
        enableClientRandom = ApplicationConfig.getInstance().get("${mqtt.client.enableClientRandom}", enableClientRandom);
        completionTimeout = ApplicationConfig.getInstance().get("${mqtt.client.completionTimeout}", completionTimeout);
        keepalive = ApplicationConfig.getInstance().get("${mqtt.client.keepalive}", keepalive);
        connect();
    }

    public void connect() {
        String id = clientId;
        if(enableClientRandom) {
            id = String.format("%s_%s", clientId, UUID.randomUUID().toString().replace("-", ""));
        }

        try {
            client = new MqttClient(host, id, new MemoryPersistence());
            MqttConnectOptions option = new MqttConnectOptions();
            option.setCleanSession(true);
            option.setUserName(username);
            option.setPassword(password.toCharArray());
            option.setConnectionTimeout(completionTimeout);
            option.setKeepAliveInterval(keepalive);
            option.setAutomaticReconnect(true);

            client.setCallback(callback);
            client.connect(option);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 发布
     * @param topic
     * @param data
     * @param qos
     * @param retained
     */
    public void publish(String topic, String data, int qos, boolean retained) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(data.getBytes());
        MqttTopic mqttTopic = client.getTopic(topic);
        if(null == mqttTopic) {
            log.error("Topic Not Exist");
        }
        MqttDeliveryToken token;
        try {
            token = mqttTopic.publish(message);
            token.waitForCompletion();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 订阅某个主题
     * @param topic
     * @param qos
     */
    public void subscribe(String topic, int qos) {
        try {
            client.subscribe(topic, qos);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}