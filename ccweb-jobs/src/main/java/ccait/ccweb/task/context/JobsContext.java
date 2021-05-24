package ccait.ccweb.task.context;

import ccait.ccweb.config.LangConfig;
import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.context.UserContext;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.UserGroupRoleModel;
import ccait.ccweb.model.UserModel;
import com.esotericsoftware.reflectasm.MethodAccess;
import entity.query.core.ApplicationConfig;
import entity.tool.util.FastJsonUtils;
import entity.tool.util.JsonUtils;
import entity.tool.util.ReflectionUtils;
import entity.tool.util.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ccait.ccweb.utils.StaticVars.LOGIN_KEY;

@Component
public class JobsContext {

    private static final Logger log = LoggerFactory.getLogger( JobsContext.class );

    private volatile static Map<String, Method> methodMap = new HashMap<>();

    private volatile static UserModel jobsUser;

    @Value("${ccweb.jobs.user.username:username}")
    private String username;

    @Value("${ccweb.jobs.user.userkey:userkey}")
    private String userkey;

    @PostConstruct
    void init() {
        username = ApplicationConfig.getInstance().get("${ccweb.jobs.user.username}", username);
        userkey = ApplicationConfig.getInstance().get("${ccweb.jobs.user.userkey}", userkey);
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(userkey)) {
            return;
        }

        if(jobsUser != null) {
            return;
        }

        synchronized (this) {
            UserModel user = new UserModel();
            user.setUsername(username);
            user.setKey(userkey);
            try {
                jobsUser = user.where("username=#{username}").and("[key]=#{key}").first();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static UserModel getJobsUser() {
        return jobsUser;
    }

    public static void checkLogin(HttpServletRequest request) throws Exception {
        UserModel user = CCApplicationContext.getSession(request, LOGIN_KEY, UserModel.class);
        if(user == null) {
            throw new HttpResponseException(HttpStatus.UNAUTHORIZED.value(), LangConfig.getInstance().get("login_please"));
        }
    }

    public static List<UserGroupRoleModel> getUserGroupRoles(HttpServletRequest request) throws Exception {
        UserModel user = CCApplicationContext.getSession(request, LOGIN_KEY, UserModel.class);
        if(user == null) {
            throw new HttpResponseException(HttpStatus.UNAUTHORIZED.value(), LangConfig.getInstance().get("login_please"));
        }

        return getUserGroupRoles(request, user);
    }

    public static List<UserGroupRoleModel> getUserGroupRoles(HttpServletRequest request, UserModel user) throws Exception {
        if(user.getUserGroupRoleModels() == null || user.getUserGroupRoleModels().size() < 1) {
            List<UserGroupRoleModel> userGroupRoleModelList = UserContext.getUserGroupRoleModels(request, user.getUserId());
            userGroupRoleModelList.stream().forEach((item) -> {
                item.getGroup();
                item.getRole();
            });
            user.setUserGroupRoleModels(userGroupRoleModelList);
        }

        return user.getUserGroupRoleModels();
    }

    public void register(String id, Method method) {
        methodMap.put(id, method);
    }

    public static void check(@RequestBody ScheduleEntity scheduleEntity) throws Exception {
        if(StringUtils.isEmpty(scheduleEntity.getId())) {
            throw new Exception("id can not be empty!");
        }

        if(StringUtils.isEmpty(scheduleEntity.getService())) {
            throw new Exception("service can not be empty!");
        }

        if(StringUtils.isEmpty(scheduleEntity.getCron())) {
            throw new Exception("cron can not be empty!");
        }
    }

    public static void invoke(String id, Object[] args) throws IOException {
        if(!methodMap.containsKey(id)) {
            return;
        }

        Method m = methodMap.get(id);
        Class<?> type = m.getDeclaringClass();
        String beanName = type.getSimpleName().substring(0, 1).toLowerCase() + type.getSimpleName().substring(1);
        Object obj = CCApplicationContext.getInstance().getBean(beanName);
        if(obj == null) {
            obj = ReflectionUtils.getInstance(type);
        }

        if(args == null || args.length == 0) {
            ReflectionUtils.invoke(type, obj, m.getName());
            return;
        }

        if(m.getParameterTypes() != null && m.getParameterTypes().length>0) {
            MethodAccess access = ReflectionUtils.getMethodAccess(type);
            int index = access.getIndex(m.getName(), m.getParameterTypes());
            List objs = new ArrayList();
            for(int i=0; i<m.getParameterTypes().length; i++) {
                if(i>=args.length) {
                    objs.add(null);
                    continue;
                }

                if(m.getParameterTypes()[i].isPrimitive() || String.class.equals(m.getParameterTypes()[i])) {
                    objs.add(StringUtils.cast(m.getParameterTypes()[i], args[i].toString()));
                }
                else {
                    Object task = JsonUtils.convert(args[i], m.getParameterTypes()[i]);
                    objs.add(task);
                }
            }
            access.invoke(obj, index, objs.toArray());
            return;
        }

        ReflectionUtils.invoke(type, obj, m.getName(), args);
    }
}
