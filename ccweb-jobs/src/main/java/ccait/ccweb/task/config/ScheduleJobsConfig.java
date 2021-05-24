package ccait.ccweb.task.config;


import ccait.ccweb.client.JobsClient;
import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.task.annotation.CCJobs;
import ccait.ccweb.task.context.JobsContext;
import entity.query.core.ApplicationConfig;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import static ccait.ccweb.utils.StaticVars.LOG_PRE_SUFFIX;

@Component
public class ScheduleJobsConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger( ScheduleJobsConfig.class );

    @Autowired
    JobsClient jobsClient;

    @Value("${spring.application.name:}")
    String service;

    @Value("${ccweb.schedule.table:schedule}")
    String tablename;

    @Autowired
    JobsContext jobsContext;

    @PostConstruct
    private void postConstruct() {
        service = ApplicationConfig.getInstance().get("${spring.application.name}", "service");
        tablename = ApplicationConfig.getInstance().get("${ccweb.schedule.table}", "schedule");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ApplicationContext app = CCApplicationContext.getInstance();
        if (app == null) {
            throw new RuntimeException("程序启动顺序不正确, ApplicationContext必须优先启动！");
        }

        Map<String, Object> map = app.getBeansWithAnnotation(EnableScheduling.class);

        CCApplicationContext.ensureTable(ScheduleEntity.class, tablename);

        for (String key : map.keySet()) {
            try {
                Object schedule = map.get(key);

                Class<?> clazz = schedule.getClass();

                Method[] methods = clazz.getMethods();
                for (Method m : methods) {
                    Method method;
                    try {
                        method = schedule.getClass().getMethod(m.getName(), m.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        log.error(LOG_PRE_SUFFIX + e);
                        continue;
                    }

                    if (method == null) {
                        continue;
                    }

                    CCJobs ann = m.getAnnotation(CCJobs.class);
                    if (ann == null) {
                        continue;
                    }

                    if(StringUtils.isEmpty(ann.id()) || StringUtils.isEmpty(ann.cron())) {
                        continue;
                    }

                    jobsClient.register(new ScheduleEntity(){{
                        setId(ann.id());
                        setService(service);
                        setCron(ann.cron());
                        setArgs(ann.args());
                        setMaxThreads(ann.maxThreads());
                        setThreadPoolName(ann.threadPoolName());
                        setMaxJobs(ann.maxJobs());
                    }});

                    jobsContext.register(ann.id(), method);
                }
            } catch (Exception e) {
                log.error(LOG_PRE_SUFFIX + "ScheduleJobsConfig init error =======>", e);
            }
        }
    }
}
