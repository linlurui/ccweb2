package ccait.ccweb.entites;

import entity.query.Queryable;
import entity.query.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@Tablename("${ccweb.schedule.table}")
public class ScheduleEntity extends Queryable<ScheduleEntity> implements Serializable {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCron() {
        return cron.replace("×", "*");
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(Integer maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Integer getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(Integer maxJobs) {
        this.maxJobs = maxJobs;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public int getRunTotal() {
        return runTotal;
    }

    public void setRunTotal(int runTotal) {
        this.runTotal = runTotal;
    }

    @PrimaryKey
    @Fieldname(value = "id")
    private String id;

    @Fieldname(value = "service")
    private String service;

    @Fieldname(value = "cron")
    private String cron;

    @Fieldname(value = "exclusive")
    private boolean exclusive;

    @Fieldname(value = "maxThreads")
    private int maxThreads;

    @Fieldname(value = "maxJobs")
    private int maxJobs;

    @Fieldname(value = "threadPoolName")
    private String threadPoolName;

    @Fieldname(value = "running")
    private int running;

    @Fieldname(value = "runTotal")
    private int runTotal;

    @Exclude
    private String[] args;

    @Exclude
    private boolean ack;
}
