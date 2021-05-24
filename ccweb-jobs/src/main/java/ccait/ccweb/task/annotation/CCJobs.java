package ccait.ccweb.task.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CCJobs {
    String id() default "";
    String cron() default "";
    String[] args() default {};
    String threadPoolName() default "";
    int maxThreads() default 15;
    int maxJobs() default 0;
    boolean ask() default false;
}
