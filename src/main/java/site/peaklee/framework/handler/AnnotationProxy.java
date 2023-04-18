package site.peaklee.framework.handler;

import site.peaklee.framework.pojo.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author PeakLee
 * @version 2023
 * @serial AnnotationProxy
 * @since 2023/4/10
 */
public interface AnnotationProxy {

    void error(Method method,Exception e);

    default void invoke(MethodProxy proxy, Object... args){
        Method method = proxy.getMethod();
        try {
            method.invoke(proxy.getTarget(),args);
        }catch (Exception e){
            error(method,e);
        }
    }
}
