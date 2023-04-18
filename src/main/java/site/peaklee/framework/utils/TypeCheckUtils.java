package site.peaklee.framework.utils;

import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;
import site.peaklee.framework.core.spi.SpringInject;
import site.peaklee.framework.server.impl.SpiBeanManager;

/**
 * @author PeakLee
 * @version 2023
 * @serial TypeCheckUtils
 * @since 2023/4/2
 */
public class TypeCheckUtils {

    public static Boolean conflictOrNot(Class<?> clazz){
        if (SpringInject.class.isAssignableFrom(clazz)){
            for (Class<?> aClass : SpiBeanManager.CLASSES) {
                if (aClass.isAssignableFrom(clazz)){
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean isSharable(Class<?> clazz){
        if (ChannelHandler.class.isAssignableFrom(clazz)) {
            return clazz.isAnnotationPresent(ChannelHandler.Sharable.class);
        }
        return false;
    }

    public static Boolean isComponent(Class<?> clazz){
        if (ChannelHandler.class.isAssignableFrom(clazz)) {
            return clazz.isAnnotationPresent(Component.class);
        }
        return false;
    }

    public static Boolean isSpringInject(Class<?> clazz){
        return SpringInject.class.isAssignableFrom(clazz);
    }
}
