package ccait.ccweb.task.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CCJobs {
    String id() default "";
    String cron() default "";
}
