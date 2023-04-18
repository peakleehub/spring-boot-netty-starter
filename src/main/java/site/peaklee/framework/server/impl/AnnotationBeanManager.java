package site.peaklee.framework.server.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import site.peaklee.framework.annotation.handler.*;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.pojo.MethodProxy;
import site.peaklee.framework.utils.ClsUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial AnnotationBeanManager
 * @since 2023/4/10
 */
@Slf4j
public final class AnnotationBeanManager implements Ordered,CommandLineRunner,ApplicationContextAware {
    private static final Class<? extends Annotation>[] CLASSES= new Class[]{
            OnActive.class,
            OnAddSession.class,
            OnBeforeMessage.class,
            OnComplete.class,
            OnError.class,
            OnIdle.class,
            OnInactive.class,
            OnMessage.class,
            OnRegister.class,
            OnRemoveSession.class,
            OnUnregister.class,
            OnBeforeAddSession.class,
            OnAfterMessage.class
    };

    private final Map<String,Set<MethodProxy>> annotations;

    private ApplicationContext applicationContext;

    public AnnotationBeanManager() {
        this.annotations = new HashMap<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void run(String... args) {
        SocketAutoConfiguration configuration = applicationContext.getBean(SocketAutoConfiguration.class);
        if (applicationContext!=null && applicationContext.getBeanDefinitionCount()>0){
            String[] names = applicationContext.getBeanDefinitionNames();
            for (String name : names) {
                Object bean = applicationContext.getBean(name);
                for (Class<? extends Annotation> aClass : CLASSES) {
                    Set<MethodProxy> methods = ClsUtils.matchMethodProxyAnnotation(bean,bean.getClass(),aClass);
                    if (configuration.getApplicationType().equals(AppType.CLIENT) && typesIgnored(aClass)){
                        log.warn("When using the client, it will not be able to provide automatic registration services, and its functionality will become invalid.");
                        continue;
                    }
                    if (annotations.containsKey(aClass.getName())){
                        annotations.get(aClass.getName()).addAll(methods);
                    }else {
                        annotations.put(aClass.getName(), methods);
                    }
                }
            }
        }
    }
    public Set<MethodProxy> getCache(Class<? extends Annotation> clazz){
        if (annotations.containsKey(clazz.getName())){
            return annotations.get(clazz.getName());
        }
        return null;
    }

    private Boolean typesIgnored(Class<?> clazz){
        Class<?>[] Ignored= new Class[]{
                OnAddSession.class,
                OnRemoveSession.class,
                OnBeforeAddSession.class};
        return Arrays.stream(Ignored).allMatch(e -> e.getName().equalsIgnoreCase(clazz.getName()));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
