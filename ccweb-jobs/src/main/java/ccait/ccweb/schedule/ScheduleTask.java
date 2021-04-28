package ccait.ccweb.schedule;

import ccait.ccweb.client.CCWebJobsClient;
import ccait.ccweb.entites.ScheduleEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务线程类
 **/
public class ScheduleTask implements Runnable {

    @Autowired
    CCWebJobsClient jobsClient;

    public ScheduleEntity getScheduleInfo() {
        return scheduleEntity;
    }

    private ScheduleEntity scheduleEntity;

    public ScheduleTask(ScheduleEntity entity) {
        this.scheduleEntity = entity;
    }

    @Override
    public void run() {
        jobsClient.run(scheduleEntity);
    }
}
