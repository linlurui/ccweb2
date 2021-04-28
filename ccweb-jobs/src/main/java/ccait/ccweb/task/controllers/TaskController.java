package ccait.ccweb.task.controllers;

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
@RequestMapping( value = {"api/task"} )
public class TaskController {

    private ResponseData success = new ResponseData(){{
        setMessage("success");
    }};

    /***
     * 执行任务
     * @param scheduleEntity
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "run", method = RequestMethod.POST )
    public ResponseData run(@RequestBody ScheduleEntity scheduleEntity) throws Exception {

        ScheduleJobsContext.check(scheduleEntity);

        ScheduleJobsContext.invoke(scheduleEntity.getId());

        return success;
    }
}
