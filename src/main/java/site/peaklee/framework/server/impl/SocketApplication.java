package site.peaklee.framework.server.impl;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.context.SocketConfigurableContext;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.core.SpringSocketApplication;
import site.peaklee.framework.core.spi.ApplicationDestroy;
import site.peaklee.framework.core.spi.SpringCommandLineRunner;
import site.peaklee.framework.core.spi.SpringInject;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.enums.HandlerEvent;
import site.peaklee.framework.enums.HandlerType;
import site.peaklee.framework.enums.ProtoType;
import site.peaklee.framework.handler.impl.*;
import site.peaklee.framework.pojo.HandlerCallback;
import site.peaklee.framework.server.Server;
import site.peaklee.framework.session.impl.ApplicationSession;
import site.peaklee.framework.utils.IOCUtils;
import site.peaklee.framework.utils.TypeCheckUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author PeakLee
 * @version 2023
 * @serial ServerApplication
 * @since 2023/4/11
 */

@Slf4j
public abstract class SocketApplication implements SocketConfigurableContext {

    protected static final String THREAD_NAME_TEP="socket-%d";

    protected static AtomicBoolean IS_START = new AtomicBoolean(Boolean.FALSE);

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    protected final SocketAutoConfiguration configuration;

    protected final Set<HandlerBean> handlers;

    protected final ConfigurableApplicationContext context;

    protected ApplicationSession serverSession;

    protected Consumer<ChannelFuture> successful;

    protected Consumer<Server> closed;

    protected Consumer<ApplicationSession> closeBefore;

    protected Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener;

    protected final SpiBeanManager destroyBean;

    protected final String[] command;

    protected final AnnotationBeanManager annotationBeanManager;

    protected List<HandlerBean> inbound;
    protected List<HandlerBean> decoder;
    protected List<HandlerBean> encoder;
    protected List<HandlerBean> outbound;
    protected List<HandlerBean> protoc;

    protected final Set<String> packages;

    protected abstract void start(Consumer<ChannelFuture> callback);

    protected abstract void initBootstrap();

    protected abstract  void initHandler();

    protected abstract void closeBootstrap();

    protected abstract void loadFirstHandler(ChannelPipeline pipeline);

    protected abstract void loadLastHandler(ChannelPipeline pipeline);

    protected abstract void loadProtocCoder(ChannelPipeline pipeline,MessageLite instance);

    protected abstract void startSuccessful(String format);

    protected abstract void startFailed(Channel channel);

    protected abstract void startError(Channel channel,Throwable e);

    protected SocketApplication(ConfigurableApplicationContext context, Set<HandlerBean> handlers, Set<String> packages, String[] command) {
        SocketAutoConfiguration configuration = context.getBean(SocketAutoConfiguration.class);
        this.annotationBeanManager = context.getBean(AnnotationBeanManager.class);
        this.packages = packages;
        this.command = command;
        this.context = context;
        this.configuration = configuration;
        this.handlers = handlers;
        this.destroyBean = SpiBeanManager.initInstance(packages, context);
        this.readHandler();
        this.checkConfig();
        this.registryThis();
    }

    private void checkConfig() {
        ProtoType supportProtobuf = this.configuration.getSupportProtobuf();
        if (supportProtobuf == ProtoType.PROTOBUF){
            if (protoc==null || protoc.isEmpty()){
                throw new NullPointerException("If you use protobuf as the transport protocol, please add an annotation handler to the protobuf class and set the type to PROTOC");
            }
        }
        if (supportProtobuf == ProtoType.CUSTOM){
            if (decoder==null||decoder.isEmpty()||encoder==null||encoder.isEmpty()){
                throw new NullPointerException("If you use custom as the transport protocol, please add an annotation handler to the decoder or encoder class and set the type to decoder or encoder.");
            }
        }
    }

    private void readHandler(){
        this.inbound = handlers.stream().filter(bean -> bean.getType().equals(HandlerType.INBOUND) && ChannelHandler.class.isAssignableFrom(bean.getHandlerClass())).sorted(Comparator.comparing(HandlerBean::getOrder)).collect(Collectors.toList());
        this.decoder = handlers.stream().filter(bean -> bean.getType().equals(HandlerType.DECODE) && ChannelHandler.class.isAssignableFrom(bean.getHandlerClass())).sorted(Comparator.comparing(HandlerBean::getOrder)).collect(Collectors.toList());
        this.encoder = handlers.stream().filter(bean -> bean.getType().equals(HandlerType.ENCODE) && ChannelHandler.class.isAssignableFrom(bean.getHandlerClass())).sorted(Comparator.comparing(HandlerBean::getOrder)).collect(Collectors.toList());
        this.outbound = handlers.stream().filter(bean -> bean.getType().equals(HandlerType.OUTBOUND) && ChannelHandler.class.isAssignableFrom(bean.getHandlerClass())).sorted(Comparator.comparing(HandlerBean::getOrder)).collect(Collectors.toList());
        this.protoc = handlers.stream().filter(bean -> bean.getType().equals(HandlerType.PROTOC)).sorted(Comparator.comparing(HandlerBean::getOrder)).collect(Collectors.toList());
    }

    private void registryThis(){
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)this.context.getBeanFactory();
        if (beanFactory.containsBean("site.peaklee.framework.socketServer")){
            beanFactory.removeBeanDefinition("site.peaklee.framework.socketServer");
        }
        beanFactory.registerSingleton("site.peaklee.framework.socketServer", this);
    }

    public synchronized void startApplication(){
        if (!IS_START.get()){
            this.start(channelFuture -> {
                if (successful!=null){
                    successful.accept(channelFuture);
                }
            });
        }else {
            log.error("The current application has been started. If you need to manually start it, please set the auto start property bit to false");
        }
    }

    protected void initializerChannel(ChannelPipeline pipeline){
        DefaultHandlerCacheHelper instance = DefaultHandlerCacheHelper.getInstance();
        if (configuration.getEnableLog()){
            pipeline.addLast("site.peaklee.framework.loggerHandler", new LoggingHandler(configuration.getLogLevel()));
        }
        if (configuration.getApplicationType().equals(AppType.SERVER)){
            ChannelHandler cache = instance.getCache(SocketSessionRegisteredHandler.class.getName(), new SocketSessionRegisteredHandler(configuration.getServer().getAutoRegisterSession(), packages, annotationBeanManager));
            pipeline.addLast("site.peaklee.framework.socketSessionRegisteredHandler", cache);
        }
        loadFirstHandler(pipeline);
        loadCoderHandler(pipeline);
        if (configuration.getServer().getEnableIdle() && configuration.getApplicationType().equals(AppType.SERVER)){
            pipeline.addLast("site.peaklee.framework.idleStateHandler",new IdleStateHandler(
                    configuration.getServer().getReaderIdleTime(),
                    configuration.getServer().getWriterIdleTime(),
                    configuration.getServer().getIdleTime(),
                    configuration.getServer().getIdleUnit()));
        }
        ChannelHandler cache = instance.getCache(SocketInterceptHandler.class.getName(), new SocketInterceptHandler(packages));
        pipeline.addLast("site.peaklee.framework.socketInterceptHandler",cache);
        if (handlerListener!=null && !handlerListener.isEmpty()){
            ChannelHandler handler = instance.getCache(SocketListenerHandler.class.getName(), new SocketListenerHandler(handlerListener));
            pipeline.addLast("site.peaklee.framework.SocketListenerHandler",handler);
        }
        ChannelHandler handler = instance.getCache(SocketAnnotationHandler.class.getName(), new SocketAnnotationHandler(annotationBeanManager));
        pipeline.addLast("site.peaklee.framework.SocketAnnotationHandler", handler);
        ChannelHandler before = instance.getCache(SocketBeforeMessageHandler.class.getName(), new SocketBeforeMessageHandler(packages, annotationBeanManager, handlerListener));
        pipeline.addLast("site.peaklee.framework.SocketBeforeMessageHandler",before);
        ChannelHandler message = instance.getCache(SocketMessageHandler.class.getName(), new SocketMessageHandler(packages, annotationBeanManager, handlerListener));
        pipeline.addLast("site.peaklee.framework.SocketMessageHandler",message);
        if (inbound!=null){
            assembling(pipeline, inbound);
        }
        if (outbound!=null){
            assembling(pipeline, outbound);
        }
        ChannelHandler after = instance.getCache(SocketAfterMessageHandler.class.getName(), new SocketAfterMessageHandler(packages, annotationBeanManager, handlerListener));
        pipeline.addLast("site.peaklee.framework.SocketAfterMessageHandler",after);
        loadLastHandler(pipeline);
        if (configuration.getApplicationType().equals(AppType.SERVER)){
            ChannelHandler unregister = instance.getCache(SocketSessionUnregisteredHandler.class.getName(), new SocketSessionUnregisteredHandler(configuration.getServer().getAutoRegisterSession(),packages,annotationBeanManager));
            pipeline.addLast("site.peaklee.framework.socketSessionUnregisteredHandler", unregister);
        }
    }

    protected void assembling(ChannelPipeline pipeline, List<HandlerBean> handlerBeans){
        for (HandlerBean handlerBean : handlerBeans) {
            try {
                ChannelHandler handler = (ChannelHandler)handlerBean.getHandlerClass().newInstance();
                if (TypeCheckUtils.isSharable(handlerBean.getHandlerClass()) &&
                        (TypeCheckUtils.isComponent(handlerBean.getHandlerClass()) ||
                TypeCheckUtils.isSpringInject(handlerBean.getHandlerClass()))){
                    if (IOCUtils.hasBean(handlerBean.getHandlerClass())){
                        handler = (ChannelHandler) IOCUtils.getBean(handlerBean.getHandlerClass());
                    }else {
                        IOCUtils.registerBean(handlerBean.getHandlerClass(), handler);
                    }
                }
                if (TypeCheckUtils.isSpringInject(handlerBean.getHandlerClass())){
                    ((SpringInject) handler).setContext(context);
                }
                IOCUtils.injectBeanHandler(handler);
                IOCUtils.injectAttrHandler(handler);
                pipeline.addLast(handlerBean.getName(), handler);
            }catch (InstantiationException|IllegalAccessException e){
                log.info("initialize load service processor {} - {}",handlerBean.getName(),handlerBean.getHandlerClass().getSimpleName());
            }
        }
    }

    protected MessageLite loadMessageLite(HandlerBean handlerBean){
        try {
            MethodType type = MethodType.methodType(handlerBean.getInnerClass());
            MethodHandle getDefaultInstance = MethodHandles.lookup().findStatic(handlerBean.getInnerClass(), "getDefaultInstance", type);
            return (MessageLite) getDefaultInstance.invoke();
        }catch (Throwable e){
            return null;
        }
    }


    protected void assemblingProtobuf(ChannelPipeline pipeline){
        for (HandlerBean handlerBean : protoc) {
            MessageLite instance = loadMessageLite(handlerBean);
            if (instance==null){
                log.warn("The provided protobuf is internally incorrect, it is not a class of type Message Lite.");
                continue;
            }
            loadProtocCoder(pipeline, instance);
        }
    }

    protected synchronized void bindStartListener(ChannelFuture bind,Consumer<ChannelFuture> callback){
        try {
            this.serverSession = null;
            this.serverSession = ApplicationSession.create(bind.channel(),bind);
            bind.addListener(future -> {
                if (future.isSuccess()) {
                    if (callback != null) {
                        callback.accept(bind);
                    }
                    if (IOCUtils.hasBeans(SpringCommandLineRunner.class)) {
                        Collection<SpringCommandLineRunner> values = context.getBeansOfType(SpringCommandLineRunner.class).values();
                        List<SpringCommandLineRunner> collect = values.stream().sorted(Comparator.comparingInt(SpringCommandLineRunner::getOrder)).collect(Collectors.toList());
                        for (SpringCommandLineRunner value : collect) {
                            value.run(this);
                        }
                    }
                    SpringSocketApplication.global_watch.stop();
                    double seconds = SpringSocketApplication.global_watch.getTotalTimeSeconds();
                    NUMBER_FORMAT.setMaximumFractionDigits(3);
                    NUMBER_FORMAT.setRoundingMode(RoundingMode.UP);
                    startSuccessful(NUMBER_FORMAT.format(seconds));
                } else {
                    IS_START.set(false);
                    startFailed(bind.channel());
                }
            });
        }catch (Throwable e){
            IS_START.set(false);
            startError(bind.channel(),e);
        }
    }

    protected void loadCoderHandler(ChannelPipeline pipeline) {
        ProtoType supportProtobuf = this.configuration.getSupportProtobuf();
        switch (supportProtobuf){
            case STRING:
                pipeline.addLast("site.peaklee.framework.stringDecoder", new StringDecoder())
                        .addLast("site.peaklee.framework.stringEncoder", new StringEncoder());
                break;
            case PROTOBUF:
                assemblingProtobuf(pipeline);
                break;
            default:
                assembling(pipeline, decoder);
                assembling(pipeline, encoder);
                break;
        }
    }

    @Override
    public void successful(Consumer<ChannelFuture> callback) {
        this.successful = callback;
    }

    @Override
    public void closed(Consumer<Server> callback) {
        this.closed = callback;
    }

    @Override
    public void closeBefore(Consumer<ApplicationSession> callback) {
        this.closeBefore = callback;
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return this.context;
    }

    @Override
    public ApplicationSession getApplicationSession() {
        return this.serverSession;
    }

    @Override
    public String[] getArgs() {
        return this.command;
    }

    @Override
    public void addHandlerListener(HandlerEvent handlerEvent, Consumer<HandlerCallback> callback) {
        if (this.handlerListener==null){
            this.handlerListener = new ConcurrentHashMap<>();
        }
        if (!this.handlerListener.containsKey(handlerEvent)){
            this.handlerListener.put(handlerEvent,new LinkedList<>());
        }
        this.handlerListener.get(handlerEvent).add(callback);
    }

    @Override
    public void destroy() {
        Set<ApplicationDestroy> cache = destroyBean.getCache(ApplicationDestroy.class);
        if (this.closeBefore!=null){
            this.closeBefore.accept(this.serverSession);
        }
        if (!cache.isEmpty()){
            for (ApplicationDestroy applicationDestroy : cache) {
                applicationDestroy.beforeDestroy(this.serverSession);
            }
        }
        this.serverSession.close();
        this.closeBootstrap();
        if (!cache.isEmpty()){
            for (ApplicationDestroy applicationDestroy : cache) {
                applicationDestroy.destroy(this);
            }
        }
        if (this.closed!=null){
            this.closed.accept(this);
        }
        IS_START.set(false);
    }
}
