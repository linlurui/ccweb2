package ccait.ccweb.entites;

import entity.query.Queryable;
import entity.query.annotation.AutoIncrement;
import entity.query.annotation.Fieldname;
import entity.query.annotation.PrimaryKey;
import entity.query.annotation.Tablename;

@Tablename("${ccweb.schedule.table:schedule}")
public class ScheduleEntity extends Queryable<ScheduleEntity> {
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
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    @Fieldname(value = "id")
    private String id;

    @Fieldname(value = "service")
    private String service;

    @Fieldname(value = "cron")
    private String cron;
}
