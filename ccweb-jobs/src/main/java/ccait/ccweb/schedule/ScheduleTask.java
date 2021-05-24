package ccait.ccweb.schedule;

import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import ccait.ccweb.task.context.JobsContext;
import ccait.ccweb.utils.EncryptionUtil;
import ccait.ccweb.utils.JwtUtils;
import entity.query.core.ApplicationConfig;
import entity.tool.util.JsonUtils;
import entity.tool.util.RequestUtils;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.regex.Pattern;

import static ccait.ccweb.utils.StaticVars.DEFAULT_AUTHORIZATION;
import static ccait.ccweb.utils.StaticVars.DEFAULT_USERKEY;

/**
 * 定时任务线程类
 **/
@Component
public class ScheduleTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger( ScheduleTask.class );

    public ScheduleEntity getScheduleInfo() {
        return scheduleEntity;
    }

    private ScheduleEntity scheduleEntity;

    public ScheduleTask(ScheduleEntity entity) {
        this.scheduleEntity = entity;
    }

    @Override
    public void run() {
        try {
            HashMap<String, String> header = getAuthHeader();
            RequestUtils.post(getServer() + "/api/jobs/run", JsonUtils.toJson(scheduleEntity),
                    header);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getServer() {
        String serverUrl = String.format("http://localhost:%s", ApplicationConfig.getInstance().get("${server.port}", "80"));
        if(StringUtils.isEmpty(ApplicationConfig.getInstance().get("${ccweb.jobs.server}", ""))) {
            return serverUrl;
        }

        if(!Pattern.matches("https?://[^:/]+(:\\d+)?", ApplicationConfig.getInstance().get("${ccweb.jobs.server}", ""))) {
            return serverUrl;
        }

        return ApplicationConfig.getInstance().get("${ccweb.jobs.server}", serverUrl);
    }

    protected HashMap<String, String> getAuthHeader() {
        String tokenKey = ApplicationConfig.getInstance().get("${ccweb.auth.header}", DEFAULT_AUTHORIZATION);
        if(StringUtils.isEmpty(tokenKey)) {
            return null;
        }

        Boolean jwtEnable = Boolean.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.jwt.enable}", "false"));
        Boolean aesEnable = Boolean.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.aes.enable}", "false"));
        Long jwtMillis = Long.valueOf(ApplicationConfig.getInstance().get("${ccweb.auth.user.jwt.millis}", "6000000"));
        String aesPublicKey = ApplicationConfig.getInstance().get("${ccweb.security.encrypt.AES.publicKey}", "ccait");

        UserModel user = JobsContext.getJobsUser();
        String token = "";
        HashMap<String, String> header = new HashMap<String, String>();
        if(jwtEnable) {
            token = JwtUtils.createJWT(jwtMillis, user);
            user.setJwtToken(token);
            header.put(tokenKey, token);
            return header;
        }

        else if(aesEnable) {
            if(StringUtils.isEmpty(user.getKey())) {
                return null;
            }
            String vaildCode2 = null;
            try {
                vaildCode2 = EncryptionUtil.md5(EncryptionUtil.encryptByAES(user.getUserId().toString(), user.getKey() + aesPublicKey), "UTF-8");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                log.error(e.getMessage(), e);
                return null;
            }
            token = EncryptionUtil.encryptByAES(user.getUsername() + vaildCode2, aesPublicKey);
            user.setAesToken(token);
            header.put(tokenKey, token);
            String userkey = ApplicationConfig.getInstance().get("${ccweb.auth.userkey}", DEFAULT_USERKEY);
            header.put(userkey, user.getKey());

            return header;
        }

        return null;
    }
}
