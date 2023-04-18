package site.peaklee.framework.server.impl;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.core.spi.*;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.utils.IOCUtils;
import site.peaklee.framework.utils.ScanUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeakLee
 * @version 2023
 * @serial SpiBeanManager
 * @since 2023/3/30
 */
@Slf4j
public final class SpiBeanManager {

    public static final Class<?>[] CLASSES =
            {SocketRegistered.class,
                    SocketUnregistered.class,
                    SocketActive.class,
                    SocketInactive.class,
                    SocketIdle.class,
                    SocketExceptionCapture.class,
                    SocketBeforeReadMessage.class,
                    SocketCompleteMessage.class,
                    SocketUnregisteredSession.class,
                    SocketRegisteredSession.class,
                    ApplicationDestroy.class,
                    SocketMessage.class,
                    SocketAfterReadMessage.class};

    private final Map<String, Map<String, Object>> cacheMap = new ConcurrentHashMap<>();



    private SpiBeanManager() {
    }

    public static SpiBeanManager initInstance(Set<String> packages, ApplicationContext context) {
        SocketAutoConfiguration configuration = context.getBean(SocketAutoConfiguration.class);
        SpiBeanManager spiBeanManager = new SpiBeanManager();
        for (Class<?> aClass : CLASSES) {
            Set<Class<?>> classes = new HashSet<>();
            for (String aPackage : packages) {
                classes.addAll(ScanUtil.getInstance().getInterfaceClasses(aPackage, aClass));
            }
            ConcurrentHashMap<String, Object> item = new ConcurrentHashMap<>();
            if (!classes.isEmpty()) {
                for (Class<?> itemClass : classes) {
                    if (!itemClass.isInterface()) {
                        if (configuration.getApplicationType().equals(AppType.CLIENT) && typesIgnored(itemClass)){
                            log.warn("When using the client, it will not be able to provide automatic registration services, and its functionality will become invalid.");
                            continue;
                        }
                        try {
                            if (spiBeanManager.findCache(itemClass) == null) {
                                Object o;
                                if (IOCUtils.hasBean(itemClass)) {
                                    o = context.getBean(itemClass);
                                }else {
                                    o = itemClass.newInstance();
                                    IOCUtils.markExecution(o);
                                    if (SpringInject.class.isAssignableFrom(itemClass) && !ChannelHandler.class.isAssignableFrom(itemClass)) {
                                        IOCUtils.registerBean(itemClass, o);
                                        ((SpringInject) o).setContext(context);
                                    }
                                }
                                IOCUtils.injectBeanHandler( o);
                                IOCUtils.injectAttrHandler( o);
                                item.put(itemClass.getName(), o);
                            } else {
                                Object cache = spiBeanManager.findCache(itemClass);
                                item.put(itemClass.getName(), cache);
                            }
                        } catch (Exception e) {
                            log.error("Cannot instantiate a class of type {} because: {}", itemClass.getSimpleName(), e.getMessage());
                        }
                    }

                }
            }
            spiBeanManager.cacheMap.put(aClass.getName(), item);
        }
        return spiBeanManager;
    }

    private static Boolean typesIgnored(Class<?> clazz){
        Class<?>[] Ignored= new Class[]{
                SocketUnregisteredSession.class,
                SocketRegisteredSession.class};
        return Arrays.stream(Ignored).allMatch(e -> e.getName().equalsIgnoreCase(clazz.getName()));
    }

    public <T> Set<T> getCache(Class<T> cls) {
        if (cacheMap.containsKey(cls.getName())) {
            Map<String, Object> stringObjectMap = cacheMap.get(cls.getName());
            return new HashSet<>((Collection<? extends T>) stringObjectMap.values());
        }
        return new HashSet<>();
    }

    public Object findCache(Class<?> cls) {
        for (Map.Entry<String, Map<String, Object>> mapEntry : cacheMap.entrySet()) {
            if (mapEntry.getValue().containsKey(cls.getName())) {
                return mapEntry.getValue().get(cls.getName());
            }
        }
        return null;
    }
}
