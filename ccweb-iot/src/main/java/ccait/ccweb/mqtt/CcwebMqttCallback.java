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

import ccait.ccweb.context.ApplicationContext;
import ccait.ccweb.context.EntityContext;
import ccait.ccweb.utils.EncryptionUtil;
import entity.query.ColumnInfo;
import entity.query.Queryable;
import entity.query.core.ApplicationConfig;
import entity.query.enums.AlterMode;
import entity.tool.util.JsonUtils;
import entity.tool.util.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * MQTT 推送回调
 * @author linlurui
 * @date 2018-08-22
 */
@Component
public class CcwebMqttCallback implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(CcwebMqttCallback.class);

    @Value("${mqtt.client.charset:UTF-8}")
    private String charset = "UTF-8";

    @Value("${entity.security.encrypt.AES.publicKey:ccait}")
    private String aesPublicKey;

    @Value("${entity.table.reservedField.createBy:createBy}")
    private String createByField;

    @Autowired
    private CcwebMqttClient client;

    @PostConstruct
    private void init() {
        charset = ApplicationConfig.getInstance().get("${mqtt.client.charset}", charset);
        aesPublicKey = ApplicationConfig.getInstance().get("${entity.security.encrypt.AES.publicKey}", aesPublicKey);
        createByField = ApplicationConfig.getInstance().get("${entity.table.reservedField.createBy}", createByField);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.info("CcwebMqttClient断开连接，准备重连....");
        client.connect();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("deliveryComplete---------" + token.isComplete());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        handleMessage(topic, message);
    }

    public void handleMessage(String topic, MqttMessage message) {
        try {
            String msg = new String(message.getPayload(), charset);
            log.info("Mqtt Topic: " + topic);
            log.info("Mqtt Message: " + msg);
            Map<String, Object> data = JsonUtils.parse(msg, Map.class);
            List<String> args = StringUtils.splitString2List(topic, "/");
            String datasource = args.get(0);
            String table = args.get(1);
            topic = args.get(2);
            Integer userid = Integer.parseInt(EncryptionUtil.decryptByAES(args.get(3).toLowerCase(), aesPublicKey));
            data.put(createByField, userid);
            data.put("topic_name", topic);
            List<ColumnInfo> cloumns = null;
            if(ApplicationContext.existTable(datasource, table)) {
                cloumns = ApplicationContext.ensureColumns(datasource, table, data);
            }
            else {
                cloumns = ApplicationContext.ensureTable(data, datasource, table);
            }

            List<String> fieldList = cloumns.stream().map(a->a.getColumnName()).collect(Collectors.toList());
            Queryable entity = (Queryable) EntityContext.getEntity(table, fieldList);
            entity.insert();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}