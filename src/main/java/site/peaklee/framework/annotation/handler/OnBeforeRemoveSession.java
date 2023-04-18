package site.peaklee.framework.annotation.handler;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial OnBeforeRemoveSession
 * @since 2023/4/10
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnBeforeRemoveSession {
}
