package ccait.ccweb.entites;

import entity.query.Queryable;
import entity.query.annotation.AutoIncrement;
import entity.query.annotation.Fieldname;
import entity.query.annotation.PrimaryKey;

public class AppConfig extends Queryable<AppConfig> {

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @AutoIncrement
    @PrimaryKey
    private Integer id;
    private String service;
    private String key;
    private String value;
}
