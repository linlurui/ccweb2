package ccait.ccweb.entites;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class TaskSchedulerInfo {

    public TaskSchedulerInfo() {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        scheduledFutureMap = new HashMap<>();
    }

    public ThreadPoolTaskScheduler getThreadPoolTaskScheduler() {
        return threadPoolTaskScheduler;
    }

    public ScheduledFuture<?> getScheduledFuture(String id) {
        if(!scheduledFutureMap.containsKey(id)) {
            scheduledFutureMap.put(id, new FutureInfo());
        }

        return scheduledFutureMap.get(id).getFuture();
    }

    public void setScheduledFuture(String id, ScheduledFuture<?> scheduledFuture) {
        if(!scheduledFutureMap.containsKey(id)) {
            scheduledFutureMap.put(id, new FutureInfo());
        }

        scheduledFutureMap.get(id).setFuture(scheduledFuture);
    }

    public void removeFuture(String id) {
        if(scheduledFutureMap.containsKey(id)) {
            scheduledFutureMap.remove(id);
        }
    }

    public void clear() {
        scheduledFutureMap.clear();
    }

    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private Map<String, FutureInfo> scheduledFutureMap;

    class FutureInfo {
        public ScheduledFuture<?> getFuture() {
            return future;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        private ScheduledFuture<?> future;
    }
}
