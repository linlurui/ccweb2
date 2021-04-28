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
import ccait.ccweb.context.UserContext;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import entity.query.core.ApplicationConfig;
import entity.tool.util.JsonUtils;
import entity.tool.util.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static ccait.ccweb.utils.StaticVars.*;


@RestController
@RequestMapping(value = { "api" })
public class AuthController extends AbstractWebController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Value("${ccweb.auth.user.jwt.millis:600000}")
    private long jwtMillis;

    @Value("${ccweb.auth.user.jwt.enable:false}")
    private boolean jwtEnable;

    @Value("${ccweb.auth.user.aes.enable:false}")
    private boolean aesEnable;

    @Value("${ccweb.auth.user.wechat.enable:false}")
    private boolean wechatEnable;

    @Value("${ccweb.ip.whiteList:}")
    private String whiteListText;

    @Value("${ccweb.ip.blackList:}")
    private String blackListText;

    @Value("${ccweb.security.encrypt.AES.publicKey:ccait}")
    private String aesPublicKey;

    @Value("${ccweb.auth.user.wechat.secret:}")
    private String secret;

    @Value("${ccweb.auth.user.wechat.appid:}")
    private String appid;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @PostConstruct
    private void construct() {
        whiteListText = ApplicationConfig.getInstance().get("${ccweb.ip.whiteList}", whiteListText);
        blackListText = ApplicationConfig.getInstance().get("${ccweb.ip.blackList}", blackListText);
        jwtEnable = ApplicationConfig.getInstance().get("${ccweb.auth.user.jwt.enable}", jwtEnable);
        aesEnable = ApplicationConfig.getInstance().get("${ccweb.auth.user.aes.enable}", aesEnable);
        wechatEnable = ApplicationConfig.getInstance().get("${ccweb.auth.user.wechat.enable}", wechatEnable);
        aesPublicKey = ApplicationConfig.getInstance().get("${ccweb.security.encrypt.AES.publicKey}", aesPublicKey);
        secret = ApplicationConfig.getInstance().get("${ccweb.auth.user.wechat.secret}", secret);
        appid = ApplicationConfig.getInstance().get("${ccweb.auth.user.wechat.appid}", appid);
    }

    /***
     * login
     * @return
     */
    @ResponseBody
    @RequestMapping( value = "login", method = RequestMethod.POST  )
    public ResponseData userLogin(@RequestBody UserModel user) {
        try {
            user = this.login(user);

            return success(user);

        } catch (Exception e) {
            log.error(LOG_PRE_SUFFIX + "ERROR=====> ", e);

            return error(150, e);
        }
    }

    /***
     * logout
     * @return
     */
    @ResponseBody
    @RequestMapping( value = "logout", method = RequestMethod.GET  )
    public ResponseData logout() {

        UserContext.logout(request);

        return success();
    }

    /***
     * user login
     * @param user
     * @return
     * @throws Exception
     */
    public UserModel login(UserModel user) throws Exception {

        user = UserContext.login(user.getUsername(), md5(user.getPassword()), request, response);

        return user;
    }

    public boolean wechatLogin(String code) throws IOException {
        if(!wechatEnable) {
            throw new RuntimeException("Can not support wechat!!!");
        }

        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);

        String result = RequestUtils.get(url);

        Map<String, String> map = JsonUtils.parse(result, Map.class);

        return false;
    }
}
