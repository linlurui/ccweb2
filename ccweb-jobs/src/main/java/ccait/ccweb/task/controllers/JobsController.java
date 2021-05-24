package ccait.ccweb.task.controllers;

import ccait.ccweb.annotation.AccessCtrl;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.schedule.ScheduleTask;
import ccait.ccweb.task.context.JobsContext;
import ccait.ccweb.utils.ScheduleUtils;
import entity.tool.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@AccessCtrl
@RestController
@Order(-666666)
@RequestMapping( value = {"api/jobs"} )
public class JobsController {

    @Autowired
    protected HttpServletRequest request;

    private ResponseData success = new ResponseData(){{
        setMessage("success");
    }};

    private static final Logger log = LoggerFactory.getLogger( JobsController.class );

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

        try {
            JobsContext.checkLogin(request);
            JobsContext.check(scheduleEntity);

            if (scheduleEntity.getMaxJobs() > 0 && ScheduleUtils.running(scheduleEntity) >= scheduleEntity.getMaxJobs()) {
                return new ResponseData() {{
                    setMessage("be long max jobs!");
                    setStatus(500);
                }};
            }

            if (StringUtils.isEmpty(scheduleEntity.getService())) {
                throw new Exception("service name can not be empty!");
            }

            if(scheduleEntity.getRunTotal()>0 &&
                    scheduleEntity.where("id=#{id}")
                            .and("runTotal>0")
                            .and("runTotal>=#{runTotal}")
                            .exist()) {

                scheduleEntity = scheduleEntity.where("id=#{id}").first();
                if(scheduleEntity.getRunning() < 1) {
                    ScheduleUtils.cancel(new ScheduleTask(scheduleEntity));
                }

                return success;
            }

            scheduleEntity.setRunning(1);
            scheduleEntity.where("id=#{id}").and("service=#{service}").update("running=running+#{running}");
            scheduleEntity.where("id=#{id}").update("runTotal=runTotal+1");
            JobsContext.invoke(scheduleEntity.getId(), scheduleEntity.getArgs());
            if (!scheduleEntity.isAck()) {
                scheduleEntity.setRunning(-1);
                scheduleEntity.where("id=#{id}").and("service=#{service}").and("running>0").update("running=running+#{running}");
            }
        }  catch(Exception e) {
            log.error("fail to run job: " + e.getMessage(), e);
            throw e;
        }

        return success;
    }

    /***
     * 手动提交任务
     * @param scheduleEntity
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "commit", method = RequestMethod.POST )
    public ResponseData commit(@RequestBody ScheduleEntity scheduleEntity) throws Exception {

        JobsContext.checkLogin(request);
        if(StringUtils.isEmpty(scheduleEntity.getThreadPoolName())) {
            scheduleEntity.setThreadPoolName(ScheduleUtils.DefaultThreadPoolName);
        }

        if(StringUtils.isEmpty(scheduleEntity.getId())) {
            throw new Exception("id can not be empty!");
        }

        if(StringUtils.isEmpty(scheduleEntity.getService())) {
            throw new Exception("service name can not be empty!");
        }

        scheduleEntity.setRunning(-1);
        scheduleEntity.where("id=#{id}").and("service=#{service}").and("running>0").update("running=running+#{running}");
        return success;
    }
}
