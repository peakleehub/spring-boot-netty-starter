package site.peaklee.framework.annotation;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial InjectBean
 * @since 2023/3/30
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectBean {

    String value() default "";

    boolean require() default true;
}
