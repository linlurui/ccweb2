package ccait.ccweb.task.context;

import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.entites.ScheduleEntity;
import entity.tool.util.ReflectionUtils;
import entity.tool.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class ScheduleJobsContext {

    private volatile static Map<String, Method> methodMap = new HashMap<>();

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

    public static void invoke(String id) {
        if(!methodMap.containsKey(id)) {
            return;
        }

        Method m = methodMap.get(id);
        Class<?> type = m.getDeclaringClass();
        String beanName = type.getSimpleName().substring(0, 1).toLowerCase() + type.getSimpleName().substring(1);
        Object obj = CCApplicationContext.getInstance().getBean(beanName);
        ReflectionUtils.invoke(type, obj, m.getName());
    }
}
