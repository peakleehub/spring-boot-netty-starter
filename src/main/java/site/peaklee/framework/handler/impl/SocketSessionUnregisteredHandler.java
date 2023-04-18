package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.annotation.handler.OnBeforeRemoveSession;
import site.peaklee.framework.annotation.handler.OnRemoveSession;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.core.spi.SocketUnregisteredSession;
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
 * @serial SocketOutboundSession
 * @since 2023/3/31
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketSessionUnregisteredHandler extends ThreadLocalInboundHandlerAdapter implements AdapterSessionManager, AnnotationProxy, HandlerInitProxy {

    private Set<SocketUnregisteredSession> socketRemoveSessions;

    private final Boolean autoRegistered;

    private final Set<MethodProxy> beforeMethods;

    private final Set<MethodProxy> methods;

    public SocketSessionUnregisteredHandler(Boolean autoRegistered, Set<String> packages, AnnotationBeanManager annotationBeanManager) {
        super(packages);
        this.autoRegistered = autoRegistered;
        this.beforeMethods = annotationBeanManager.getCache(OnBeforeRemoveSession.class);
        this.methods = annotationBeanManager.getCache(OnRemoveSession.class);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Session withCreate=null;
        if (this.autoRegistered){
            withCreate = findWithCreate(ctx);
        }
        if (socketRemoveSessions!=null && !socketRemoveSessions.isEmpty()) {
            for (SocketUnregisteredSession socketUnregistered : socketRemoveSessions) {
                log.debug("Execute socketUnregistered method:{}", socketUnregistered.getClass());
                socketUnregistered.removeBefore(withCreate);
            }
        }
        if (beforeMethods!=null && !beforeMethods.isEmpty()){
            for (MethodProxy proxy : beforeMethods) {
                invoke(proxy, withCreate);
            }
        }

        if (this.autoRegistered && withCreate!=null){
            withCreate = deregisterSession(withCreate.getId());
        }

        if (methods!=null && !methods.isEmpty()){
            for (MethodProxy proxy : methods) {
                invoke(proxy, withCreate);
            }
        }

        if (socketRemoveSessions!=null && !socketRemoveSessions.isEmpty()) {
            for (SocketUnregisteredSession socketUnregistered : socketRemoveSessions) {
                log.debug("Execute socketUnregistered method:{}",socketUnregistered.getClass());
                socketUnregistered.removed(withCreate);
            }
        }

        if (withCreate!=null){
            log.info("The client with ID {} has been offline.",withCreate.getId());
        }
        ctx.fireChannelUnregistered();
    }

    @Override
    public void error(Method method, Exception e) {
        log.error("Cannot call method with name {} because: {}",method.getName(),e.getMessage());
    }

    @Override
    public void init() {
        SpiBeanManager spiBeanManager = getLocal();
        this.socketRemoveSessions = spiBeanManager.getCache(SocketUnregisteredSession.class);
    }
}
