package site.peaklee.framework.annotation;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial InjectAttr
 * @since 2023/3/30
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectAttr {
    String value();
}
