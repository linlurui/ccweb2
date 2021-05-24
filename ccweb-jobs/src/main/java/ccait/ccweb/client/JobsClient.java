package ccait.ccweb.client;

import ccait.ccweb.config.FeignConfig;
import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import ccait.ccweb.task.config.JobsUserConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "ccweb-jobs", path = "api", fallback = JobsFallback.class, configuration = { FeignConfig.class, JobsUserConfig.class })
public interface JobsClient {
    @RequestMapping( value = "schedule/register", method = RequestMethod.POST )
    ResponseData register(@RequestBody ScheduleEntity scheduleEntity);

    @RequestMapping( value = "schedule/cancel", method = RequestMethod.POST )
    ResponseData cancel(@RequestBody ScheduleEntity scheduleEntity);

    @RequestMapping( value = "schedule/clear", method = RequestMethod.POST )
    ResponseData clear();

    @RequestMapping( value = "jobs/run", method = RequestMethod.POST )
    ResponseData run(@RequestBody ScheduleEntity scheduleEntity);

    @RequestMapping( value = "commit", method = RequestMethod.POST )
    ResponseData commit(@RequestBody ScheduleEntity scheduleEntity);
}
