package site.peaklee.framework.annotation;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial EnableSocket
 * @since 2023/3/27
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerScan {
    /**
     * 需要扫描的包路径名
     */
    String[] packages();
}
