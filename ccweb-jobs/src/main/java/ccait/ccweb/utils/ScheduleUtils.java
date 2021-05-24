package ccait.ccweb.utils;

import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.entites.TaskSchedulerInfo;
import ccait.ccweb.schedule.ScheduleTask;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronTrigger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/***
 * 定时任务工具类
 */
public class ScheduleUtils {

    private static final Logger log = LoggerFactory.getLogger( ScheduleUtils.class );

    public final static String DefaultThreadPoolName = "DefaultThreadPool";

    public final static Integer DefaultThreadPool = 15;

    private static Map<String, TaskSchedulerInfo> scheduledMap = new ConcurrentHashMap<>();

    /**
     * 启动
     * @param scheduleTask 定时任务
     */
    public static boolean start(ScheduleTask scheduleTask) throws Exception {
        if(StringUtils.isEmpty(scheduleTask.getScheduleInfo().getThreadPoolName())) {
            scheduleTask.getScheduleInfo().setThreadPoolName(DefaultThreadPoolName);
        }

        if(StringUtils.isEmpty(scheduleTask.getScheduleInfo().getId())) {
            throw new Exception("id can not be empty!");
        }

        if(scheduleTask.getScheduleInfo().getMaxThreads() < 1) {
            scheduleTask.getScheduleInfo().setMaxThreads(DefaultThreadPool);
        }
        log.info("【ccweb-jobs】启动定时任务线程 taskId: " + scheduleTask.getScheduleInfo().getId() + ", ThreadPoolName: " +
                scheduleTask.getScheduleInfo().getThreadPoolName());

        if(!scheduledMap.containsKey(scheduleTask.getScheduleInfo().getThreadPoolName())) {
            scheduledMap.put(scheduleTask.getScheduleInfo().getThreadPoolName(), new TaskSchedulerInfo());
            scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName()).getThreadPoolTaskScheduler().setPoolSize(scheduleTask.getScheduleInfo().getMaxThreads());
            scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName()).getThreadPoolTaskScheduler().initialize();
        }

        ScheduledFuture<?> scheduledFuture = scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName()).getThreadPoolTaskScheduler()
                .schedule(scheduleTask, new CronTrigger(scheduleTask.getScheduleInfo().getCron()));
        scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName()).setScheduledFuture(scheduleTask.getScheduleInfo().getId(), scheduledFuture);
        return true;
    }

    /**
     * 清除
     */
    public static void clear() throws Exception {

        if(scheduledMap.size() == 0) {
            return;
        }

        for(Map.Entry<String, TaskSchedulerInfo> item : scheduledMap.entrySet()) {
            item.getValue().clear();
        }

        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.where("1=1").delete();
    }

    /**
     * 取消
     * @param scheduleTask 定时任务
     */
    public static boolean cancel(ScheduleTask scheduleTask) throws Exception {

        if(StringUtils.isEmpty(scheduleTask.getScheduleInfo().getThreadPoolName())) {
            scheduleTask.getScheduleInfo().setThreadPoolName(DefaultThreadPoolName);
        }

        if(StringUtils.isEmpty(scheduleTask.getScheduleInfo().getId())) {
            throw new Exception("id can not be empty!");
        }

        if(scheduledMap.size() == 0) {
            return false;
        }
        log.info("【ccweb-jobs】关闭定时任务线程 taskId: " + scheduleTask.getScheduleInfo().getId() + ", ThreadPoolName: " +
                scheduleTask.getScheduleInfo().getThreadPoolName());

        ScheduledFuture<?> scheduledFuture = scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName())
                .getScheduledFuture(scheduleTask.getScheduleInfo().getId());

        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
        scheduledMap.get(scheduleTask.getScheduleInfo().getThreadPoolName()).removeFuture(scheduleTask.getScheduleInfo().getId());

        ScheduleEntity scheduleEntity = scheduleTask.getScheduleInfo();
        if(scheduleEntity.where("id=#{id}").and("service=#{service}").exist()) {
            scheduleEntity.where("id=#{id}").and("service=#{service}").delete();
        }

        return true;
    }

    /**
     * 重设
     * @param scheduleTask 定时任务
     */
    public static void reset(ScheduleTask scheduleTask) throws Exception {
        //先取消定时任务
        cancel(scheduleTask);
        //然后启动新的定时任务
        start(scheduleTask);
    }

    /***
     * 获取正在执行任务数
     * @param scheduleEntity
     * @throws Exception
     */
    public static Integer running(ScheduleEntity scheduleEntity) throws Exception {
        if(StringUtils.isEmpty(scheduleEntity.getThreadPoolName())) {
            scheduleEntity.setThreadPoolName(DefaultThreadPoolName);
        }

        if(StringUtils.isEmpty(scheduleEntity.getId())) {
            throw new Exception("id can not be empty!");
        }

        if(StringUtils.isEmpty(scheduleEntity.getService())) {
            throw new Exception("service name can not be empty!");
        }

        scheduleEntity = scheduleEntity.where("id=#{id}").and("service=#{service}").first();
        if(scheduleEntity == null) {
            throw new Exception("can not find the schedule profile!");
        }

        return scheduleEntity.getRunning();
    }
}
