package ccait.ccweb.utils;

import ccait.ccweb.schedule.ScheduleTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/***
 * 定时任务工具类
 */
public class ScheduleUtils {

    private static final Logger log = LoggerFactory.getLogger( ScheduleUtils.class );

    private static ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private static Map<String, ScheduledFuture<?>> scheduledFutureMap = new HashMap<>();

    static {
        threadPoolTaskScheduler.initialize();
        log.info("定时任务线程池启动");
    }

    /**
     * 启动
     * @param scheduleTask 定时任务
     */
    public static boolean start(ScheduleTask scheduleTask) {
        log.info("启动定时任务线程 taskId: " + scheduleTask.getScheduleInfo().getId());
        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler
                .schedule(scheduleTask, new CronTrigger(scheduleTask.getScheduleInfo().getCron()));
        scheduledFutureMap.put(scheduleTask.getScheduleInfo().getId(), scheduledFuture);
        return true;
    }

    /**
     * 取消
     * @param scheduleTask 定时任务
     */
    public static boolean cancel(ScheduleTask scheduleTask) {
        log.info("关闭定时任务线程 taskId: " + scheduleTask.getScheduleInfo().getId());
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(scheduleTask.getScheduleInfo().getId());
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
        scheduledFutureMap.remove(scheduleTask.getScheduleInfo().getId());
        return true;
    }

    /**
     * 重设
     * @param scheduleTask 定时任务
     */
    public static void reset(ScheduleTask scheduleTask) {
        //先取消定时任务
        cancel(scheduleTask);
        //然后启动新的定时任务
        start(scheduleTask);
    }
}
