package ccait.ccweb.schedule.controllers;

import ccait.ccweb.annotation.AccessCtrl;
import ccait.ccweb.config.LangConfig;
import ccait.ccweb.context.CCApplicationContext;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.model.UserModel;
import ccait.ccweb.schedule.ScheduleTask;
import ccait.ccweb.task.context.JobsContext;
import ccait.ccweb.utils.ScheduleUtils;
import entity.query.core.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import static ccait.ccweb.utils.StaticVars.LOGIN_KEY;

@AccessCtrl
@RestController
@Order(-666666)
@RequestMapping( value = {"api/schedule"} )
public class ScheduleController {

    @Value("${ccweb.security.admin.username:admin}")
    private String admin;

    @Autowired
    protected HttpServletRequest request;

    private ResponseData success;

    @PostConstruct
    void init() {
        admin = ApplicationConfig.getInstance().get("${ccweb.security.admin.username}", admin);
        success = new ResponseData(){{
            setMessage("success");
        }};
    }

    /***
     * 注册计划任务
     * @param scheduleEntity
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "register", method = RequestMethod.POST )
    public ResponseData register(@RequestBody ScheduleEntity scheduleEntity) throws Exception {

        JobsContext.checkLogin(request);
        JobsContext.check(scheduleEntity);
        if(!scheduleEntity.where("id=#{id}").and("service=#{service}").exist()) {
            scheduleEntity.insert();
        }
        else {
            if(scheduleEntity != null) {
                scheduleEntity.where("id=#{id}").and("service=#{service}").update("cron=#{cron}",
                        "exclusive=#{exclusive}", "maxThreads=#{maxThreads}", "maxJobs=#{maxJobs}", "threadPoolName=#{threadPoolName}");
            }
        }

        ScheduleUtils.reset(new ScheduleTask(scheduleEntity));

        return success;
    }

    /***
     * 取消任务
     * @param scheduleEntity
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "cancel", method = RequestMethod.POST )
    public ResponseData cancel(@RequestBody ScheduleEntity scheduleEntity) throws Exception {

        JobsContext.checkLogin(request);
        JobsContext.check(scheduleEntity);

        ScheduleUtils.cancel(new ScheduleTask(scheduleEntity));

        return success;
    }

    /***
     * 清除任务
     * @return
     * @throws Exception
     */
    @ResponseBody
    @AccessCtrl
    @RequestMapping( value = "clear", method = RequestMethod.POST )
    public ResponseData clear() throws Exception {

        JobsContext.checkLogin(request);

        UserModel user = CCApplicationContext.getSession(request, LOGIN_KEY, UserModel.class);
        if(!admin.equals(user.getUsername())) {
            throw new Exception(LangConfig.getInstance().get("you_are_not_administrator"));
        }

        ScheduleUtils.clear();

        return success;
    }
}
