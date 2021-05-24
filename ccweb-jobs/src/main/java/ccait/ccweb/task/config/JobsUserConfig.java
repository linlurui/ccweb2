package ccait.ccweb.task.config;

import ccait.ccweb.model.UserModel;
import ccait.ccweb.task.context.JobsContext;
import ccait.ccweb.utils.EncryptionUtil;
import ccait.ccweb.utils.JwtUtils;
import entity.query.core.ApplicationConfig;
import entity.tool.util.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static ccait.ccweb.utils.StaticVars.DEFAULT_AUTHORIZATION;

public class JobsUserConfig implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger( JobsUserConfig.class );


    @Override
    public void apply(RequestTemplate requestTemplate) {
        String tokenKey = ApplicationConfig.getInstance().get("${ccweb.auth.header}", DEFAULT_AUTHORIZATION);
        if(StringUtils.isEmpty(tokenKey)) {
            return;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        if(StringUtils.isNotEmpty(request.getHeader(tokenKey))) {
            return;
        }

        Boolean jwtEnable = Boolean.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.jwt.enable}", "false"));
        Boolean aesEnable = Boolean.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.aes.enable}", "false"));
        Long jwtMillis = Long.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.jwt.millis}", "6000000"));
        String aesPublicKey = ApplicationConfig.getInstance().get("${ccweb.security.encrypt.AES.publicKey}", "ccait");

        UserModel user = JobsContext.getJobsUser();
        String token = "";
        if(jwtEnable) {
            token = JwtUtils.createJWT(jwtMillis, user);
            user.setJwtToken(token);
        }

        if(aesEnable) {
            if(StringUtils.isEmpty(user.getKey())) {
                return;
            }
            String vaildCode2 = null;
            try {
                vaildCode2 = EncryptionUtil.md5(EncryptionUtil.encryptByAES(user.getUserId().toString(), user.getKey() + aesPublicKey), "UTF-8");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                log.error(e.getMessage(), e);
                return;
            }
            token = EncryptionUtil.encryptByAES(user.getUsername() + vaildCode2, aesPublicKey);
            user.setAesToken(token);
        }

        if(StringUtils.isEmpty(token)) {
            return;
        }

        requestTemplate.header(tokenKey, token);
    }
}
