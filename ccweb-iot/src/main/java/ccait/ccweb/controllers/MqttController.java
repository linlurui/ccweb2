/**
 *
 *  License: http://www.apache.org/licenses/LICENSE-2.0
 *  Home page: https://github.com/linlurui/ccweb
 *  Note: to build on java, include the jdk1.8+ compiler symbol (and yes,
 *  I know the difference between language and runtime versions; this is a compromise).
 * @author linlurui
 * @Date Date: 2019-02-10
 */

package ccait.ccweb.controllers;

import ccait.ccweb.abstracts.AbstractWebController;
import ccait.ccweb.annotation.AccessCtrl;
import ccait.ccweb.config.LangConfig;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import ccait.ccweb.mqtt.CcwebMqttClient;
import ccait.ccweb.utils.EncryptionUtil;
import entity.query.core.ApplicationConfig;
import entity.tool.util.JsonUtils;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static ccait.ccweb.utils.StaticVars.LOG_PRE_SUFFIX;

@RestController
@RequestMapping(value = {"api/mqtt/{datasource}"})
public class MqttController  extends AbstractWebController {

    private static final Logger log = LoggerFactory.getLogger(MqttController.class);

    @Autowired
    private CcwebMqttClient mqttClient;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Value("${ccweb.security.encrypt.AES.publicKey:ccait}")
    private String aesPublicKey;

    @Value("${mqtt.client.clientId:clientId}")
    private String clientId = "clientId";

    @PostConstruct
    private void construct() {
        clientId = ApplicationConfig.getInstance().get("${mqtt.client.clientId}", clientId);
        aesPublicKey = ApplicationConfig.getInstance().get("${ccweb.security.encrypt.AES.publicKey}", aesPublicKey);
    }

    /***
     * 发布mqtt消息
     * @return
     */
    @AccessCtrl
    @ResponseBody
    @RequestMapping( value = "publish/{table}/{topic}/{qos}/{retained}", method = RequestMethod.POST  )
    public ResponseData publish(@PathVariable String datasource, @PathVariable String table,
                                @PathVariable String topic, @PathVariable Integer qos,
                                @PathVariable Integer retained, @RequestBody Map<String,String> body) {
        try{
            UserModel user = getLoginUser();
            if(user == null) {
                throw new HttpResponseException(HttpStatus.UNAUTHORIZED.value(), LangConfig.getInstance().get("login_please"));
            }

            publishMessage(datasource, table, topic, qos, retained, body, user);

            return success();
        }
        catch (Exception e) {
            log.error(LOG_PRE_SUFFIX + "ERROR=====> ", e);
            return error(e.getMessage());
        }
    }

    private void publishMessage(String datasource, String table, String topic, Integer qos, Integer retained, Map<String, String> body, UserModel user) {
        if(topic.toLowerCase().lastIndexOf("__clientid__") > 0) {
            int i = topic.toLowerCase().lastIndexOf("__clientid__");
            topic = topic.substring(0, i - 1) + clientId;
        }
        topic = String.format("/%s/%s/%s/%s", datasource, table, topic,
                EncryptionUtil.encryptByAES(user.getUserId().toString(), aesPublicKey));
        mqttClient.publish(topic, JsonUtils.toJson(body), qos, retained==1);
    }
}