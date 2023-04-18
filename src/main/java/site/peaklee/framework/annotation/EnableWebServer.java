package site.peaklee.framework.annotation;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial EnableWebServer
 * @since 2023/3/29
 * <p>
 *     该注解仅在主启动类生效,使用该注解将同时启动一个Springboot-web的容器上下文
 * </p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableWebServer {
}
