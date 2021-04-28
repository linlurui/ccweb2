package ccait.ccweb.client;

import ccait.ccweb.entites.ScheduleEntity;
import ccait.ccweb.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CCWebJobsFallback implements CCWebJobsClient {

    private ResponseData responseData = null;

    @Autowired
    void init() {
        responseData = new ResponseData();
        responseData.setMessage("network error");
        responseData.setStatus(400);
    }

    @Override
    public ResponseData register(ScheduleEntity scheduleEntity) {
        return responseData;
    }

    @Override
    public ResponseData run(ScheduleEntity scheduleEntity) {
        return responseData;
    }
}