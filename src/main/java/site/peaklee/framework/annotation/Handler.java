package site.peaklee.framework.annotation;

import site.peaklee.framework.enums.HandlerType;
import site.peaklee.framework.enums.HandlerType;

import java.lang.annotation.*;

/**
 * @author PeakLee
 * @version 2023
 * @serial InboundHandler
 * @since 2023/3/28
 * <p>
 *     该注解只能在类上使用,通长用来标记的类是具有一个ChannelHandler的共性祖先的类,主要提供对netty的initChannel注册
 * </p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {

    /**
     * 该属性表示了handler的加载顺序
     */
    int value();

    /**
     * 该属性表示了handler的加载名称
     */
    String name() default "";

    HandlerType type() default HandlerType.INBOUND;

    Class<?> innerClass() default Void.class;
}
