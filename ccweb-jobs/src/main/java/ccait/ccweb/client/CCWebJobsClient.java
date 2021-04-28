package ccait.ccweb.client;

import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "ccweb-jobs", path = "api", fallback = CCWebJobsFallback.class)
public interface CCWebJobsClient {
    @RequestMapping( value = "schedule/register", method = RequestMethod.POST )
    ResponseData register(@RequestBody ScheduleEntity scheduleEntity);

    @RequestMapping( value = "task/run", method = RequestMethod.POST )
    public ResponseData run(@RequestBody ScheduleEntity scheduleEntity);
}
