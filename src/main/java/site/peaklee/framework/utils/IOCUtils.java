package site.peaklee.framework.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.annotation.InjectAttr;
import site.peaklee.framework.annotation.InjectBean;
import site.peaklee.framework.config.SocketAutoConfiguration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PeakLee
 * @version 2023
 * @serial IOCUtils
 * @since 2023/3/30
 */
@Slf4j
public class IOCUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    private static final String EXPRESSION = "\\$\\{(?<express>[\\w\\d-_&$@,.:]+)}";

    private static final Map<String,Boolean> INJECT_EXECUTED = new ConcurrentHashMap<>();

    public static void markExecution(Object o){
        String key = String.format("%s@%d", o.getClass().getName(), o.hashCode());
        INJECT_EXECUTED.put(key, true);
    }

    public static Boolean isExecute(Object o){
        String key = String.format("%s@%d", o.getClass().getName(), o.hashCode());
        return INJECT_EXECUTED.containsKey(key);
    }


    public static ApplicationContext getContext(){
        return context;
    }

    public static boolean hasBean(String name) {
        try {
            context.getBean(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean hasBean(Class<?> type) {
        try {
            context.getBean(type);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }

    public static Object getBean(String name) {
        return context.getBean(name);
    }

    public static boolean hasBeans(Class<?> type) {
        try {
            context.getBeansOfType(type);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void registerBean(Class<?> aClass,Object o){
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory)((ConfigurableApplicationContext)context).getBeanFactory();
        if (factory.containsBean(aClass.getSimpleName())) {
            factory.removeBeanDefinition(aClass.getSimpleName());
        }
        factory.registerSingleton(aClass.getSimpleName(), o);
    }

    public static void injectBeanHandler(Object handler) throws IllegalAccessException {
        Set<Field> fields = ClsUtils.matchFieldsAnnotation(handler.getClass(), InjectBean.class);
        if (!fields.isEmpty()) {
            for (Field field : fields) {
                InjectBean annotation = field.getAnnotation(InjectBean.class);
                Class<?> type = field.getType();
                Object inject = null;
                if (!annotation.value().isEmpty() && context.containsBean(annotation.value())) {
                    inject = context.getBean(annotation.value());
                } else if (context.containsBean(field.getName())) {
                    inject = context.getBean(field.getName());
                } else if (context.containsBean(type.getSimpleName())) {
                    inject = context.getBean(type.getSimpleName());
                }
                if (inject == null) {
                    String[] beanNamesForType = context.getBeanNamesForType(type);
                    if (beanNamesForType.length == 1) {
                        inject = context.getBean(beanNamesForType[0]);
                    }
                }
                if (inject == null && annotation.require()) {
                    throw new NullPointerException("Unable to inject a type of " + type.getSimpleName() + " spring bean, this bean is required or try setting this " + field.getName() + " name attribute");
                }
                field.setAccessible(true);
                field.set(handler, inject);
            }
        }
    }


    public static void injectAttrHandler(Object handler) throws IllegalAccessException, InstantiationException {
        Set<Field> fields = ClsUtils.matchFieldsAnnotation(handler.getClass(), InjectAttr.class);
        if (!fields.isEmpty()) {
            for (Field field : fields) {
                InjectAttr annotation = field.getAnnotation(InjectAttr.class);
                String value = annotation.value();
                Matcher matcher = Pattern.compile(EXPRESSION).matcher(value);
                Class<?> type = field.getType();
                if (matcher.find()) {
                    String express = matcher.group("express");
                    String defaultVal = null;
                    if (express.contains(":")){
                        String[] split = express.split(":");
                        express = split[0];
                        defaultVal = split[1];
                    }
                    Object property = context.getEnvironment().getProperty(express, type);
                    if (property==null && defaultVal != null){
                        property = ClsUtils.string2Primitive(field, defaultVal);
                    }
                    field.setAccessible(true);
                    field.set(handler, property);
                }
            }
        }
    }

    public static SocketAutoConfiguration getConfig() {
        if (hasBean(SocketAutoConfiguration.class)) {
            return context.getBean(SocketAutoConfiguration.class);
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
