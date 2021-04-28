package ccait.ccweb.schedule.controllers;

import ccait.ccweb.annotation.AccessCtrl;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.schedule.ScheduleTask;
import ccait.ccweb.task.context.ScheduleJobsContext;
import ccait.ccweb.utils.ScheduleUtils;
import entity.tool.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@AccessCtrl
@RestController
@RequestMapping( value = {"api/schedule"} )
public class ScheduleController {

    private ResponseData success = new ResponseData(){{
        setMessage("success");
    }};

    /***
     * 注册任务
     * @param scheduleEntity
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "register", method = RequestMethod.POST )
    public ResponseData register(@RequestBody ScheduleEntity scheduleEntity) throws Exception {

        ScheduleJobsContext.check(scheduleEntity);
        String cron = scheduleEntity.getCron();
        scheduleEntity = scheduleEntity.where("id=#{id}").and("service=#{service}").first();
        if(scheduleEntity == null) {
            scheduleEntity.insert();
            ScheduleUtils.reset(new ScheduleTask(scheduleEntity));
        }
        else if(!cron.equals(scheduleEntity.getCron())) {
            scheduleEntity.where("id=#{id}").and("service=#{service}").update("cron=#{cron}");
            ScheduleUtils.reset(new ScheduleTask(scheduleEntity));
        }

        return success;
    }
}
