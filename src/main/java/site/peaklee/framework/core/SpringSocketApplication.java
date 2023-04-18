package site.peaklee.framework.core;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StopWatch;
import site.peaklee.framework.annotation.EnableWebServer;
import site.peaklee.framework.annotation.Handler;
import site.peaklee.framework.annotation.HandlerScan;
import site.peaklee.framework.context.SocketConfigurableContext;
import site.peaklee.framework.core.spi.SpringInject;
import site.peaklee.framework.enums.HandlerType;
import site.peaklee.framework.factory.SocketConfigurableContextFactory;
import site.peaklee.framework.utils.IOCUtils;
import site.peaklee.framework.utils.ScanUtil;
import site.peaklee.framework.utils.TypeCheckUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SpringSocketApplication
 * @since 2023/3/27
 */
@Slf4j
public final class SpringSocketApplication {

    private final Class<?> mainApplicationClass;

    private final String[] command;

    private Boolean webServer = false;

    private Set<HandlerBean> handlerClazz;

    private final Set<String> packages;

    public static final StopWatch global_watch= new StopWatch();


    public SpringSocketApplication(Class<?> mainApplicationClass,String[] command) {
        this.mainApplicationClass = mainApplicationClass;
        this.command = command;
        this.packages = new HashSet<>();
        this.parseHandler();
    }

    private void parseHandler(){
        if (this.mainApplicationClass.isAnnotationPresent(HandlerScan.class)) {
            HandlerScan annotation = this.mainApplicationClass.getAnnotation(HandlerScan.class);
            packages.addAll(Arrays.asList(annotation.packages()));
        }else {
            packages.add(this.mainApplicationClass.getPackage().getName());
        }
        if (this.mainApplicationClass.getAnnotation(EnableWebServer.class)!=null){
            this.webServer = true;
        }
        if (!packages.isEmpty()){
            handlerClazz = new HashSet<>();
            Set<Class<?>> classes=new HashSet<>();
            for (String aPackage : packages) {
                Set<Class<?>> items = ScanUtil.getInstance().getAnnotationClasses(aPackage, Handler.class);
                classes.addAll(items);
            }
            if (!classes.isEmpty()){
                for (Class<?> aClass : classes) {
                    Handler annotation = aClass.getAnnotation(Handler.class);
                    HandlerBean.HandlerBeanBuilder builder = HandlerBean.builder();
                    builder.name(aClass.getSimpleName());
                    if (!annotation.name().isEmpty()){
                        builder.name(annotation.name());
                    }
                    builder.order(annotation.value());
                    builder.handlerClass(aClass);
                    builder.type(annotation.type());
                    if (annotation.type().equals(HandlerType.PROTOC) && annotation.innerClass()==Void.class){
                        throw new NullPointerException("When selecting protocol as protobuf, the type of internal class will be required.");
                    }
                    builder.innerClass(annotation.innerClass());
                    handlerClazz.add(builder.build());
                }
            }
        }
    }

    public static SocketConfigurableContext run(Class<?> mainClass,String[] args){
        SpringSocketApplication application = new SpringSocketApplication(mainClass, args);
        return application.run();
    }


    private SocketConfigurableContext run(){
        SpringApplicationBuilder builder = new SpringApplicationBuilder(this.mainApplicationClass);
        if (!this.webServer){
            builder.web(WebApplicationType.NONE);
        }
        ConfigurableApplicationContext context = builder.run(this.command);
        global_watch.start(this.mainApplicationClass.getSimpleName());
        SocketConfigurableContext result = SocketConfigurableContextFactory.createContext(context, this.handlerClazz, packages, this.command);
        springBeanHandler(context);
        return result;
    }

    private void springBeanHandler(ConfigurableApplicationContext context){
        Set<SpringInject> injectSet = new HashSet<>();
        Set<Class<SpringInject>> classes=new HashSet<>();
        for (String aPackage : packages) {
            classes.addAll(ScanUtil.getInstance().getInterfaceTypeClasses(aPackage, SpringInject.class));
        }
        if (!classes.isEmpty()){
            for (Class<SpringInject> aClass : classes) {
                if (ChannelHandler.class.isAssignableFrom(aClass) && !TypeCheckUtils.isSharable(aClass)){
                    continue;
                }
                if (IOCUtils.hasBean(aClass)) {
                    SpringInject bean = context.getBean(aClass);
                    if (!IOCUtils.isExecute(bean)){
                        bean.setContext(context);
                    }
                    injectSet.add(bean);
                    continue;
                }
                try {
                    if (!aClass.isInterface()){
                        SpringInject springInject = aClass.newInstance();
                        IOCUtils.markExecution(springInject);
                        IOCUtils.registerBean(aClass, springInject);
                        springInject.setContext(context);
                        injectSet.add(springInject);
                    }
                }catch (Exception e){
                    log.error("Cannot initialize an instance of type {} because: {}", aClass.getName(),e.getMessage());
                }
            }
        }

        if (!injectSet.isEmpty()){
            for (SpringInject inject : injectSet) {
                try {
                    IOCUtils.injectBeanHandler(inject);
                    IOCUtils.injectAttrHandler(inject);
                }catch (Exception e){
                    log.error("cannot perform dependency injection because:{}",e.getMessage());
                }

            }
        }
    }
}
