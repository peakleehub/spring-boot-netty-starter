package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.annotation.handler.*;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.handler.AnnotationProxy;
import site.peaklee.framework.pojo.MethodProxy;
import site.peaklee.framework.server.impl.AnnotationBeanManager;
import site.peaklee.framework.session.impl.Session;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketAnnotationHandler
 * @since 2023/4/10
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketAnnotationHandler extends ChannelInboundHandlerAdapter implements AdapterSessionManager, AnnotationProxy  {

    private final AnnotationBeanManager annotationBeanManager;

    public SocketAnnotationHandler(AnnotationBeanManager annotationBeanManager) {
        this.annotationBeanManager = annotationBeanManager;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnRegister.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate);
            }
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnUnregister.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate);
            }
        }
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnActive.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate);
            }
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnInactive.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate);
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnComplete.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate);
            }
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            Session withCreate = findWithCreate(ctx);
            Set<MethodProxy> cache = annotationBeanManager.getCache(OnIdle.class);
            if (cache!=null && !cache.isEmpty()){
                for (MethodProxy proxy : cache) {
                    invoke(proxy, withCreate,evt);
                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Session withCreate = findWithCreate(ctx);
        Set<MethodProxy> cache = annotationBeanManager.getCache(OnError.class);
        if (cache!=null && !cache.isEmpty()){
            for (MethodProxy proxy : cache) {
                invoke(proxy, withCreate,cache);
            }
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void error(Method method, Exception e) {
        log.error("Cannot call method with name {} because: {}",method.getName(),e.getMessage());
    }
}
