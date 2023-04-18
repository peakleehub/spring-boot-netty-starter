package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.annotation.handler.OnAddSession;
import site.peaklee.framework.annotation.handler.OnBeforeAddSession;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.core.spi.SocketRegisteredSession;
import site.peaklee.framework.handler.AnnotationProxy;
import site.peaklee.framework.handler.HandlerInitProxy;
import site.peaklee.framework.handler.ThreadLocalInboundHandlerAdapter;
import site.peaklee.framework.pojo.MethodProxy;
import site.peaklee.framework.server.impl.AnnotationBeanManager;
import site.peaklee.framework.server.impl.SpiBeanManager;
import site.peaklee.framework.session.impl.Session;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketSessionRegisteredHandler
 * @since 2023/3/31
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketSessionRegisteredHandler extends ThreadLocalInboundHandlerAdapter implements AdapterSessionManager, AnnotationProxy, HandlerInitProxy {

    private Set<SocketRegisteredSession> socketRegisteredSessions;

    private final Boolean autoRegistered;

    private final Set<MethodProxy> addMethodProxy;

    private final Set<MethodProxy> beforeMethodProxy;


    public SocketSessionRegisteredHandler(Boolean autoRegistered, Set<String> packages,AnnotationBeanManager annotationBeanManager) {
        super(packages);
        this.autoRegistered = autoRegistered;
        this.addMethodProxy= annotationBeanManager.getCache(OnAddSession.class);
        this.beforeMethodProxy= annotationBeanManager.getCache(OnBeforeAddSession.class);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Session withCreate=null;
        if (this.autoRegistered){
            withCreate = findWithCreate(ctx);
        }
        if (socketRegisteredSessions!=null && !socketRegisteredSessions.isEmpty()) {
            for (SocketRegisteredSession registeredSession : socketRegisteredSessions) {
                log.debug("Execute socketUnregistered method:{}", registeredSession.getClass());
                registeredSession.registerBefore(withCreate);
            }
        }
        if (beforeMethodProxy!=null && !beforeMethodProxy.isEmpty()){
            for (MethodProxy proxy : beforeMethodProxy) {
                invoke(proxy, withCreate);
            }
        }

        if (this.autoRegistered && withCreate!=null){
            registrySession(withCreate);
        }

        if (addMethodProxy!=null && !addMethodProxy.isEmpty()){
            for (MethodProxy proxy : addMethodProxy) {
                invoke(proxy, withCreate);
            }
        }
        if (socketRegisteredSessions!=null && !socketRegisteredSessions.isEmpty()) {
            for (SocketRegisteredSession registeredSession : socketRegisteredSessions) {
                log.debug("Execute socketUnregistered method:{}",registeredSession.getClass());
                registeredSession.registered(withCreate);
            }
        }
        if (withCreate!=null){
            log.info("The client with ID {} is online",withCreate.getId());
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void error(Method method, Exception e) {
        log.error("Cannot call method with name {} because: {}",method.getName(),e.getMessage());
    }

    @Override
    public void init() {
        SpiBeanManager spiBeanManager = getLocal();
        this.socketRegisteredSessions = spiBeanManager.getCache(SocketRegisteredSession.class);
    }
}
